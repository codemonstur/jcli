package jcli;

import jcli.errors.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static jcli.Reflection.*;
import static org.objenesis.ObjenesisHelper.newInstance;

public enum Parser {;
    static final String FAKE_NULL = "\u0000\u0001\u0002\u0003\u0004YEAH_I_REALLY_HOPE_NO_ONE_EVER_USES_THIS_STRING";

    public static <T> T parseCommandLineArguments(final String[] args, final Class<T> clazz)
            throws InvalidArgumentConfiguration, InvalidCommandLine {
        try {
            final ArgumentMap map = new ArgumentMap(clazz);
            final T o = newInstance(clazz);
            applyArgumentsToInstance(args, map, o);
            return o;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static class ArgumentMap {

        private final Map<String, FieldAndArgument> map = new HashMap<>();

        private ArgumentMap(final Class clazz) throws NoSuchFieldException, IllegalAccessException, InvalidArgumentConfiguration {
            for (final Field field : listFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!field.isAnnotationPresent(Argument.class)) continue;

                makeAccessible(field);

                final Argument argument = field.getAnnotation(Argument.class);
                if (!isValidFieldType(field.getType()))
                    throw new InvalidArgumentType(argument);
                if (argument.name() == ' ' && argument.longName().isEmpty())
                    throw new InvalidArgumentName(field.getName());

                // FIXME test if this is really desired, maybe better to just leave the field alone with whatever was defined in the class
                if (!argument.mandatory() && argument.defaultValue().equals(FAKE_NULL))
                    throw new MissingDefaultForArgument(argument);

                addArgument(field, argument);
            }
        }

        private void addArgument(final Field field, final Argument argument) {
            if (argument.name() != ' ')
                map.put(""+argument.name(), new FieldAndArgument(field, argument));
            if (!argument.longName().isEmpty())
                map.put(argument.longName(), new FieldAndArgument(field, argument));
        }

        private static void makeAccessible(final Field field) throws IllegalAccessException, NoSuchFieldException {
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        }

        FieldAndArgument getFieldAndArgument(final String arg, final FieldAndArgument _default) {
            final FieldAndArgument remove = map.remove(arg);
            return remove != null ? remove : _default;
        }

        Collection<FieldAndArgument> remainingFieldsAndArguments() {
            return map.values();
        }
    }

    private static <T> void applyArgumentsToInstance(final String[] args, final ArgumentMap map, final T instance)
            throws InvalidCommandLine, IllegalAccessException {
        for (int i = 0; i < args.length; i++) {
            final FieldAndArgument fag = map.getFieldAndArgument(argumentToName(args[i]), null);
            if (fag == null) throw new UnknownCommandLineOption(args[i]);
            if (fag.argument.isFlag()) {
                argumentIsValidFlag(args[i]);
                fag.field.set(instance, TRUE);
                continue;
            }
            if (applyArgumentToObject(args, i, fag.field, instance)) i++;
        }

        for (final FieldAndArgument fag : map.remainingFieldsAndArguments()) {
            if (fag.argument.mandatory()) throw new MissingArgument(fag.argument);

            // FIXME this might be the better option
            // if (fag.argument.defaultValue().equals(FAKE_NULL)) continue;

            fag.field.set(instance, toFieldType(fag.field.getType(), fag.argument.defaultValue()));
        }
    }

    private static void argumentIsValidFlag(final String arg) throws LongFormFlagArgument {
        if (arg.contains("=")) throw new LongFormFlagArgument();
    }

    private static <T> boolean applyArgumentToObject(final String[] args, final int offset, final Field field, final T instance)
            throws MissingCommandLineValue, IllegalAccessException {
        final String arg = args[offset];
        final boolean longForm = isLongForm(arg);
        final String value = argumentToValue(args, offset);
        field.set(instance, toFieldType(field.getType(), value));
        return !longForm;
    }

    private static boolean isLongForm(final String arg) {
        return arg.contains("=");
    }

    private static String argumentToName(final String arg) throws InvalidCommandLine {
        if (arg.charAt(0) != '-') throw new InvalidCommandLineOption(arg);
        if (arg.startsWith("--")) {
            if (arg.length() == 2) throw new CommandLineOptionTooShort();
            final int equalsIndex = arg.indexOf('=');
            return equalsIndex == -1 ? arg.substring(2) : arg.substring(2, equalsIndex);
        }

        if (arg.length() == 1) throw new CommandLineOptionTooShort();
        int equalsIndex = arg.indexOf('=');
        if (equalsIndex == -1) {
            if (arg.length() > 2) throw new SingleDashLongFormArgument(arg);
            return arg.substring(1);
        }
        if (equalsIndex == 1) throw new InvalidCommandLineOption(arg);
        if (equalsIndex > 2) throw new SingleDashLongFormArgument(arg);
        return arg.substring(1, 2);
    }

    private static String argumentToValue(final String[] args, final int offset) throws MissingCommandLineValue {
        final String arg = args[offset];
        final int equalsIndex = arg.indexOf('=');
        if (equalsIndex == -1 && offset+1 >= args.length)
            throw new MissingCommandLineValue(arg);
        return  equalsIndex == -1 ? args[offset+1] : arg.substring(equalsIndex);
    }
}
