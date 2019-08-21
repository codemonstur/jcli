package jcli;

import jcli.errors.InvalidArgumentName;
import jcli.errors.InvalidArgumentType;
import jcli.errors.MissingArgument;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static jcli.Reflection.listFields;
import static org.objenesis.ObjenesisHelper.newInstance;

public enum Parser {;
    static final String FAKE_NULL = "\u0000\u0001\u0002\u0003\u0004YEAH_I_REALLY_HOPE_NO_ONE_EVER_USES_THIS_STRING";

    public static <T> T parseCommandLineArguments(final String[] args, final Class<T> clazz)
            throws InvalidArgumentName, MissingArgument, InvalidArgumentType {

        final T o = newInstance(clazz);

        try {
            for (final Field field : listFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!field.isAnnotationPresent(Argument.class)) continue;
                makeAccessible(field);

                final Argument annotation = field.getAnnotation(Argument.class);
                final String value = getValueForName(annotation.name(), annotation.longName(), args);
                if (value == null) {
                    if (annotation.mandatory()) throw new MissingArgument("Argument " + annotation.name() + " is mandatory");
                    if (!annotation._default().equals(FAKE_NULL)) field.set(o, toFieldType(field.getType(), annotation._default()));
                    continue;
                }

                field.set(o, toFieldType(field.getType(), value));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // can't happen, I call setAccessible
        }

        return o;
    }

    private static void makeAccessible(final Field field) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private static Object toFieldType(final Class<?> type, final String value) throws InvalidArgumentType {
        if (type.isAssignableFrom(Double.class)) return Double.valueOf(value);
        if (type.isAssignableFrom(double.class)) return Double.valueOf(value);
        if (type.isAssignableFrom(Integer.class)) return Integer.valueOf(value);
        if (type.isAssignableFrom(String.class)) return value;
        if (type.isAssignableFrom(int.class)) return Integer.valueOf(value);
        if (type.isAssignableFrom(float.class)) return Float.valueOf(value);
        if (type.isAssignableFrom(Float.class)) return Float.valueOf(value);
        if (type.isAssignableFrom(byte.class)) return Byte.valueOf(value);
        if (type.isAssignableFrom(Byte.class)) return Byte.valueOf(value);
        if (type.isAssignableFrom(char.class)) return value.charAt(0);
        if (type.isAssignableFrom(Character.class)) return value.charAt(0);
        if (type.isAssignableFrom(short.class)) return Short.valueOf(value);
        if (type.isAssignableFrom(Short.class)) return Short.valueOf(value);
        if (type.isAssignableFrom(Long.class)) return Long.valueOf(value);
        if (type.isAssignableFrom(long.class)) return Long.valueOf(value);
        if (type.isAssignableFrom(boolean.class)) return Boolean.valueOf(value);
        if (type.isAssignableFrom(Boolean.class)) return Boolean.valueOf(value);
        throw new InvalidArgumentType("The argument type " + type.getSimpleName() + " can not be used");
    }

    private static String getValueForName(final String name, final String longName, final String[] args)
            throws MissingArgument, InvalidArgumentName {

        if (name == null || name.isEmpty()) throw new InvalidArgumentName("Argument name must not be null or empty");

        final String shortName = "-"+name;
        final String realLongName = "--"+longName;
        for (int i = 0; i < args.length; i++) {
            if (shortName.equals(args[i]) || (!longName.isEmpty() && realLongName.equals(args[i]))) {
                if (i + 1 >= args.length) throw new MissingArgument("Argument " + name + " must have a value");
                return args[i+1];
            }
        }

        return null;
    }

}
