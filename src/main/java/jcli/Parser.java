package jcli;

import jcli.errors.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static jcli.Reflection.*;

public enum Parser {;
    static final String FAKE_NULL = "\u0000\u0001\u0002\u0003\u0004YEAH_I_REALLY_HOPE_NO_ONE_EVER_USES_THIS_STRING";

    public static <T> T parseCommandLineArguments(final String[] args, final Supplier<T> supplier)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        try {
            final T o = supplier.get();
            return applyArgumentsToInstance(args, toFieldAndOptionMap(o.getClass()), o);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Map<String, FieldAndOption> toFieldAndOptionMap(final Class clazz) throws InvalidOptionConfiguration {
        final Map<String, FieldAndOption> map = new HashMap<>();

        for (final Field field : listFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (!field.isAnnotationPresent(CliOption.class)) continue;
            if (Modifier.isFinal(field.getModifiers())) throw new InvalidOptionModifier(field);

            makeAccessible(field);

            final CliOption argument = field.getAnnotation(CliOption.class);
            if (!isValidFieldType(field.getType()))
                throw new InvalidOptionType(argument);
            if (argument.name() == ' ' && argument.longName().isEmpty())
                throw new InvalidOptionName(field.getName());
            if (argument.isFlag() && !isBooleanType(field))
                throw new FlagTypeNotBoolean(field);

            // FIXME test if this is really desired, maybe better to just leave the field alone with whatever was defined in the class
            if (!argument.mandatory() && argument.defaultValue().equals(FAKE_NULL))
                throw new MissingDefaultForOption(argument);

            addFieldAndOption(map, field, argument);
        }

        return map;
    }

    private static void addFieldAndOption(final Map<String, FieldAndOption> map, final Field field, final CliOption option) {
        if (option.name() != ' ')
            map.put(""+option.name(), new FieldAndOption(field, option));
        if (!option.longName().isEmpty())
            map.put(option.longName(), new FieldAndOption(field, option));
    }

    /*
     * This code used to remove the final modifier from the field using this code:
     *
     *    Field modifiersField = Field.class.getDeclaredField("modifiers");
     *    modifiersField.setAccessible(true);
     *    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
     *
     * However, since Java 12 this hack no longer works. Instead you will get an exception:
     *
     *    java.lang.NoSuchFieldException: modifiers
     *
     * There is a workaround available for Java 12 using a VarHandle documented here:
     * https://stackoverflow.com/questions/56039341/get-declared-fields-of-java-lang-reflect-fields-in-jdk12/56042394#56042394
     * VarHandles were introduced in Java 9.
     *
     * We therefore have these options:
     *
     *  1. Use the original hack, making the library unusable in Java 12 and forwards
     *  2. Use the new hack, making the library unusable in Java 8 and lower
     *  3. Don't remove the final modifier, forcing users to provide a class with mutable fields
     *
     * I have chosen 3 for now, if a different solution presents itself I will revisit the decision.
     */
    private static void makeAccessible(final Field field) {
        field.setAccessible(true);
    }

    private static <T> T applyArgumentsToInstance(final String[] args, final Map<String, FieldAndOption> map, final T instance)
            throws InvalidCommandLine, IllegalAccessException {
        final Set<CliOption> parsedOptions = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            final FieldAndOption fao = map.remove(argumentToName(args[i]));
            if (fao == null) throw new UnknownCommandLineOption(args[i]);
            parsedOptions.add(fao.option);

            if (fao.option.isFlag()) {
                isFlagArgument(args[i]);
                fao.field.set(instance, TRUE);
                continue;
            }
            if (argumentIntoObject(args, i, fao.field, instance)) i++;
        }

        for (final FieldAndOption fao : map.values()) {
            if (parsedOptions.contains(fao.option)) continue;
            if (fao.option.mandatory()) throw new MissingArgument(fao.option);

            // FIXME this might be the better option
            // if (fao.argument.defaultValue().equals(FAKE_NULL)) continue;

            fao.field.set(instance, toFieldType(fao.field.getType(), fao.option.defaultValue()));
        }

        return instance;
    }

    private static void isFlagArgument(final String argument) throws LongFormFlagArgument {
        if (argument.contains("=")) throw new LongFormFlagArgument();
    }

    private static <T> boolean argumentIntoObject(final String[] arguments, final int index, final Field field, final T instance)
            throws MissingCommandLineValue, IllegalAccessException {
        final String argument = arguments[index];
        final boolean longForm = isLongForm(argument);
        final String value = argumentToValue(arguments, index);
        field.set(instance, toFieldType(field.getType(), value));
        return !longForm;
    }

    private static boolean isLongForm(final String argument) {
        return argument.contains("=");
    }

    private static String argumentToName(final String argument) throws InvalidCommandLine {
        if (argument.charAt(0) != '-') throw new InvalidCommandLineOption(argument);
        if (argument.startsWith("--")) {
            if (argument.length() == 2) throw new CommandLineOptionTooShort();
            final int equalsIndex = argument.indexOf('=');
            return equalsIndex == -1 ? argument.substring(2) : argument.substring(2, equalsIndex);
        }

        if (argument.length() == 1) throw new CommandLineOptionTooShort();
        int equalsIndex = argument.indexOf('=');
        if (equalsIndex == -1) {
            if (argument.length() > 2) throw new SingleDashLongFormArgument(argument);
            return argument.substring(1);
        }
        if (equalsIndex == 1) throw new InvalidCommandLineOption(argument);
        if (equalsIndex > 2) throw new SingleDashLongFormArgument(argument);
        return argument.substring(1, 2);
    }

    private static String argumentToValue(final String[] arguments, final int index) throws MissingCommandLineValue {
        final String arg = arguments[index];
        final int equalsIndex = arg.indexOf('=');
        if (equalsIndex == -1 && index+1 >= arguments.length)
            throw new MissingCommandLineValue(arg);
        return  equalsIndex == -1 ? arguments[index+1] : arg.substring(equalsIndex);
    }

}
