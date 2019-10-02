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

public enum CliParser {;
    static final String FAKE_NULL = "\u0000\u0001\u0002\u0003\u0004YEAH_I_REALLY_HOPE_NO_ONE_EVER_USES_THIS_STRING";

    public static <T> T parseCommandLineArguments(final String[] args, final Supplier<T> supplier)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, supplier.get());
    }

    public static <T> T parseCommandLineArguments(final String[] args, final T object)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        try {
            return applyArgumentsToInstance(args, toFieldAndOptionMap(object.getClass()), object, Reflection::toFieldType);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseCommandLineArguments(final String[] args, final T object, final ToFieldType convert)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        try {
            return applyArgumentsToInstance(args, toFieldAndOptionMap(object.getClass()), object, convert);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, FieldAndOption> toFieldAndOptionMap(final Class clazz) throws InvalidOptionConfiguration {
        final Map<String, FieldAndOption> map = new HashMap<>();

        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliOption.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw new InvalidModifierStatic(field);
            if (Modifier.isFinal(field.getModifiers())) throw new InvalidModifierFinal(field);

            makeAccessible(field);

            final CliOption option = field.getAnnotation(CliOption.class);
            if (!isValidFieldType(field.getType()))
                throw new InvalidOptionType(option);
            if (option.name() == ' ' && option.longName().isEmpty())
                throw new InvalidOptionName(field.getName());
            if (option.isFlag() && !isBooleanType(field))
                throw new FlagTypeNotBoolean(field);
            if (option.isHelp() && !isBooleanType(field))
                throw new HelpTypeNotBoolean(field);

            // TODO test if this is really desired, maybe better to just leave the field alone with whatever was defined in the class
            if (!option.isMandatory() && !option.isFlag() && !option.isHelp() && option.defaultValue().equals(FAKE_NULL))
                throw new MissingDefaultForOption(option);

            addFieldAndOption(map, field, option);
        }

        return map;
    }

    private static void addFieldAndOption(final Map<String, FieldAndOption> map, final Field field, final CliOption option) {
        if (option.name() != ' ')
            map.put(""+option.name(), new FieldAndOption(field, option));
        if (!option.longName().isEmpty())
            map.put(option.longName(), new FieldAndOption(field, option));
    }

    private static <T> T applyArgumentsToInstance(final String[] args, final Map<String, FieldAndOption> map,
                                                  final T instance, final ToFieldType convert)
            throws InvalidCommandLine, IllegalAccessException {
        final Set<CliOption> parsedOptions = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            final FieldAndOption fao = map.remove(argumentToName(args[i]));
            if (fao == null) throw new UnknownCommandLineArgument(args[i]);
            parsedOptions.add(fao.option);

            if (fao.option.isFlag() || fao.option.isHelp()) {
                isFlagArgument(args[i]);
                fao.field.set(instance, TRUE);
                continue;
            }
            if (argumentIntoObject(args, i, fao.field, instance, convert)) i++;
        }

        for (final FieldAndOption fao : map.values()) {
            if (parsedOptions.contains(fao.option)) continue;
            if (fao.option.isMandatory()) throw new MissingArgument(fao.option);

            // TODO this might be the better option
            // if (fao.argument.defaultValue().equals(FAKE_NULL)) continue;

            fao.field.set(instance, toFieldType(fao.field.getType(), fao.option.defaultValue()));
        }

        return instance;
    }

    private static void isFlagArgument(final String argument) throws LongFormFlagArgument {
        if (argument.contains("=")) throw new LongFormFlagArgument();
    }

    private static <T> boolean argumentIntoObject(final String[] arguments, final int index, final Field field,
                                                  final T instance, final ToFieldType convert)
            throws MissingCommandLineValue, IllegalAccessException, InvalidArgumentValue {
        final String argument = arguments[index];
        final boolean longForm = isLongForm(argument);
        final String value = argumentToValue(arguments, index);
        field.set(instance, convert.toFieldType(field.getType(), value));
        return !longForm;
    }

    private static boolean isLongForm(final String argument) {
        return argument.contains("=");
    }

    private static String argumentToName(final String argument) throws InvalidCommandLine {
        if (argument.charAt(0) != '-') throw new InvalidCommandLineArgument(argument);
        if (argument.startsWith("--")) {
            if (argument.length() == 2) throw new CommandLineArgumentTooShort();
            final int equalsIndex = argument.indexOf('=');
            return equalsIndex == -1 ? argument.substring(2) : argument.substring(2, equalsIndex);
        }

        if (argument.length() == 1) throw new CommandLineArgumentTooShort();
        int equalsIndex = argument.indexOf('=');
        if (equalsIndex == -1) {
            if (argument.length() > 2) throw new SingleDashLongFormArgument(argument);
            return argument.substring(1);
        }
        if (equalsIndex == 1) throw new InvalidCommandLineArgument(argument);
        if (equalsIndex > 2) throw new SingleDashLongFormArgument(argument);
        return argument.substring(1, 2);
    }

    private static String argumentToValue(final String[] arguments, final int index) throws MissingCommandLineValue {
        final String arg = arguments[index];
        final int equalsIndex = arg.indexOf('=');
        if (equalsIndex == -1 && index+1 >= arguments.length)
            throw new MissingCommandLineValue(arg);
        return  equalsIndex == -1 ? arguments[index+1] : arg.substring(equalsIndex+1);
    }

}
