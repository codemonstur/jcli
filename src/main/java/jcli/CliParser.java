package jcli;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.*;
import jcli.model.FieldAndOption;
import jcli.model.FieldAndPosition;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static jcli.Reflection.*;
import static jcli.annotations.Constants.FAKE_NULL;

public enum CliParser {;

    public static <T> T parseCommandLineArguments(final String[] args, final Supplier<T> supplier)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, supplier.get());
    }

    public static <T> T parseCommandLineArguments(final String[] args, final T object)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        try {
            final Class<?> clazz = object.getClass();
            return applyArgumentsToInstance(args, toFieldAndOptionMap(clazz), toFieldAndPositionList(clazz), object, Reflection::toFieldType);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseCommandLineArguments(final String[] args, final T object, final ToFieldType convert)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        try {
            final Class<?> clazz = object.getClass();
            return applyArgumentsToInstance(args, toFieldAndOptionMap(clazz), toFieldAndPositionList(clazz), object, convert);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, FieldAndOption> toFieldAndOptionMap(final Class<?> clazz) throws InvalidOptionConfiguration {
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
            if (option.isHelp() && !isBooleanType(field))
                throw new HelpTypeNotBoolean(field);

            // TODO test if this is really desired, maybe better to just leave the field alone with whatever was defined in the class
            // if (hasMissingDefault(option, field)) throw new MissingDefaultForOption(option);

            addFieldAndOption(map, field, option);
        }

        return map;
    }

    private static List<FieldAndPosition> toFieldAndPositionList(final Class<?> clazz) {
        final List<FieldAndPosition> list = new ArrayList<>();

        boolean hasPositional = false;
        boolean hasOptionalPositional = false;

        int index = 0;
        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliPositional.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw new InvalidModifierStatic(field);
            if (Modifier.isFinal(field.getModifiers())) throw new InvalidModifierFinal(field);
            if (isListType(field) && hasPositional) throw new ConflictingPositionals();

            hasPositional = true;
            makeAccessible(field);

            final CliPositional positional = field.getAnnotation(CliPositional.class);
            if (hasOptionalPositional && FAKE_NULL.equals(positional.defaultValue()))
                throw new MandatoryPositionalAfterOptional();

            hasOptionalPositional = hasOptionalPositional || !FAKE_NULL.equals(positional.defaultValue());
            if (isListType(field) && !FAKE_NULL.equals(positional.defaultValue()))
                throw new DefaultForListPositional();
            if (!isValidFieldType(field.getType()))
                throw new InvalidOptionType(field);

            list.add(index++, new FieldAndPosition(field, positional));
        }

        return list;
    }

    private static void addFieldAndOption(final Map<String, FieldAndOption> map, final Field field, final CliOption option) {
        if (option.name() != ' ')
            map.put("" + option.name(), new FieldAndOption(field, option));
        if (!option.longName().isEmpty())
            map.put(option.longName(), new FieldAndOption(field, option));
    }

    private static <T> T applyArgumentsToInstance(final String[] args, final Map<String, FieldAndOption> options
            , final List<FieldAndPosition> positionals, final T instance, final ToFieldType convert) throws InvalidCommandLine, IllegalAccessException {

        initializeLists(instance);

        final int totalNumberOfPositionalArguments = positionals.size();
        final Set<CliOption> parsedOptions = new HashSet<>();

        int positionalArgumentIndex = 1;
        // Set the arguments into the object
        for (int i = 0; i < args.length; i++) {
            if (isOption(args[i])) {
                final FieldAndOption fao = options.remove(argumentToName(args[i]));
                if (fao == null) throw new UnknownCommandLineArgument(args[i]);
                parsedOptions.add(fao.option);

                if (isBooleanType(fao.field) || fao.option.isHelp()) {
                    isFlagArgument(args[i]);
                    fao.field.set(instance, TRUE);
                    continue;
                }
                if (isListType(fao.field)) {
                    final List optionList = (List)fao.field.get(instance);
                    final String argument = args[i];
                    final boolean attachedForm = isAttachedForm(argument);
                    final String value = argumentToValue(args, i);
                    optionList.add(convert.toFieldType(toParameterType(fao.field), value));
                    options.put(argumentToName(args[i]), fao);
                    if (!attachedForm) i++;
                    continue;
                }
                if (argumentIntoObject(args, i, fao.field, instance, convert)) i++;
            } else {
                // it must be a positional if its not an option
                if (positionals.isEmpty()) throw new TooManyPositionalArguments(totalNumberOfPositionalArguments, positionalArgumentIndex);
                positionalArgumentIndex++;
                if (isListType(positionals.get(0).field)) {
                    final FieldAndPosition fap = positionals.get(0);
                    final List<Object> list = (List) fap.field.get(instance);
                    final Class<?> listType = getTypeOfGenericList(fap.field, String.class);
                    final Object value = convert.toFieldType(listType, args[i]);
                    list.add(value);
                } else {
                    final FieldAndPosition fap = positionals.remove(0);
                    fap.field.set(instance, convert.toFieldType(fap.field.getType(), args[i]));
                }
            }
        }

        // check the left over options
        for (final FieldAndOption fao : options.values()) {
            if (parsedOptions.contains(fao.option)) continue;
            if (isListType(fao.field)) continue;
            if (fao.option.isMandatory()) throw new MissingArgument(fao.option);

            if (fao.option.defaultValue().equals(FAKE_NULL)) continue;

            fao.field.set(instance, toFieldType(fao.field.getType(), fao.option.defaultValue()));
        }

        // check the left over positionals
        if (positionals.size() != 1 || !isListType(positionals.get(0).field)) {
            for (final FieldAndPosition fap : positionals) {
                if (FAKE_NULL.equals(fap.position.defaultValue()))
                    throw new MissingArgument(fap.field);

                fap.field.set(instance, convert.toFieldType(fap.field.getType(), fap.position.defaultValue()));
            }
        }

        return instance;
    }

    private static boolean isOption(final String argument) {
        return !argument.isEmpty() && argument.charAt(0) == '-';
    }

    private static void isFlagArgument(final String argument) throws AttachedFormFlagArgument {
        if (argument.contains("=")) throw new AttachedFormFlagArgument();
    }

    private static <T> boolean argumentIntoObject(final String[] arguments, final int index, final Field field,
                                                  final T instance, final ToFieldType convert)
            throws MissingCommandLineValue, IllegalAccessException, InvalidArgumentValue {
        final String argument = arguments[index];
        final boolean attachedForm = isAttachedForm(argument);
        final String value = argumentToValue(arguments, index);
        field.set(instance, convert.toFieldType(field.getType(), value));
        return !attachedForm;
    }

    private static boolean isAttachedForm(final String argument) {
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
            if (argument.length() > 2) throw new SingleDashAttachedFormArgument(argument);
            return argument.substring(1);
        }
        if (equalsIndex == 1) throw new InvalidCommandLineArgument(argument);
        if (equalsIndex > 2) throw new SingleDashAttachedFormArgument(argument);
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
