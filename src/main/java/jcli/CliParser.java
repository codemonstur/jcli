package jcli;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.*;
import jcli.model.FieldAndOption;
import jcli.model.FieldAndPosition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.String.join;
import static jcli.Reflection.*;
import static jcli.Util.*;
import static jcli.annotations.Constants.FAKE_NULL;
import static jcli.errors.InvalidCommandLine.*;
import static jcli.errors.InvalidOptionConfiguration.*;
import static jcli.errors.InvalidPositionalConfiguration.*;

/**
 * The CliParser enum.
 */
public enum CliParser {;

    private static final Pattern NEGATIVE_NUMBER = Pattern.compile("-\\d+(\\.\\d+)?");
    private static final String END_OF_OPTIONS = "--";

    /**
     * parseCommandLineArguments.
     * @param <T> the type parameter
     * @param args the args
     * @param supplier the supplier
     * @return the result
      * @throws jcli.errors.InvalidCommandLine if an error occurs
     * @throws InvalidOptionConfiguration if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public static <T> T parseCommandLineArguments(final String[] args, final Supplier<T> supplier)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, supplier.get());
    }

    /**
     * Parses into an arguments class: a record is built through its canonical constructor.
      * @param <T> the type parameter
      * @param args the args
      * @param clazz the clazz
     * @return the result
     * @throws InvalidOptionConfiguration if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public static <T> T parseCommandLineArguments(final String[] args, final Class<T> clazz)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, clazz, Reflection::toFieldType);
    }
    /**
     * parseCommandLineArguments.
     * @param <T> the type parameter
     * @param args the args
     * @param clazz the clazz
     * @param convert the convert
     * @return the result
      * @throws jcli.errors.InvalidCommandLine if an error occurs
     * @throws InvalidOptionConfiguration if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public static <T> T parseCommandLineArguments(final String[] args, final Class<T> clazz, final ToFieldType convert)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, clazz, convert, Collections.emptySet());
    }
    /**
     * parseCommandLineArguments.
     * @param <T> the type parameter
     * @return the result
      * @param args the args
      * @param clazz the clazz
      * @param convert the convert
     * @param convertedTypes the convertedTypes
     * @throws InvalidOptionConfiguration if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public static <T> T parseCommandLineArguments(final String[] args, final Class<T> clazz, final ToFieldType convert
            , final Collection<Class<?>> convertedTypes) throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, clazz, new ParseContext(convertedTypes, Messages.DEFAULT
                , new ConsolePrompter(), convert, false, false, Collections.emptyMap(), Collections.emptyMap()
                , System.err::println));
    }

    /**
     * parseCommandLineArguments.
     * @param <T> the type parameter
     * @param args the args
     * @param object the object
     * @return the result
      * @throws jcli.errors.InvalidCommandLine if an error occurs
     * @throws InvalidOptionConfiguration if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public static <T> T parseCommandLineArguments(final String[] args, final T object)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, object, Reflection::toFieldType);
    }

    /**
     * parseCommandLineArguments.
     * @param <T> the type parameter
     * @param args the args
     * @param object the object
     * @param convert the convert
     * @return the result
      * @throws jcli.errors.InvalidCommandLine if an error occurs
     * @throws InvalidOptionConfiguration if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public static <T> T parseCommandLineArguments(final String[] args, final T object, final ToFieldType convert)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, object, convert, Collections.emptySet());
    }

    /**
     * parseCommandLineArguments.
     * @param <T> the type parameter
     * @return the result
      * @param args the args
      * @param object the object
      * @param convert the convert
     * @param convertedTypes the convertedTypes
     * @throws InvalidOptionConfiguration if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public static <T> T parseCommandLineArguments(final String[] args, final T object, final ToFieldType convert
            , final Collection<Class<?>> convertedTypes) throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseCommandLineArguments(args, object, new ParseContext(convertedTypes, Messages.DEFAULT
                , new ConsolePrompter(), convert, false, false, Collections.emptyMap(), Collections.emptyMap()
                , System.err::println));
    }

    static <T> T parseCommandLineArguments(final String[] args, final T object, final ParseContext context)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseWithStore(args, object.getClass(), ValueStore.forInstance(object), context);
    }

    static <T> T parseCommandLineArguments(final String[] args, final Class<T> clazz, final ParseContext context)
            throws InvalidOptionConfiguration, InvalidCommandLine {
        return parseWithStore(args, clazz, ValueStore.forClass(clazz), context);
    }

    private static <T> T parseWithStore(final String[] args, final Class<?> clazz, final ValueStore store
            , final ParseContext context) throws InvalidOptionConfiguration, InvalidCommandLine {
        final Map<String, FieldAndOption> options = toFieldAndOptionMap(clazz, context.convertedTypes
                , context.fieldConverters.keySet());
        final List<FieldAndPosition> positionals = toFieldAndPositionList(clazz, context.convertedTypes
                , context.fieldConverters.keySet());
        validateRelations(options);
        try {
            applyArguments(args, options, positionals, store, context);
            @SuppressWarnings("unchecked")
            final T built = (T) store.build();
            return built;
        } catch (final IllegalAccessException e) {
            /**
             * RuntimeException.
             * @param e the e
             * @return the result
             */
            throw new RuntimeException(e);
        }
    }

    static Map<String, FieldAndOption> toFieldAndOptionMap(final Class<?> clazz
            , final Collection<Class<?>> convertedTypes, final Collection<String> convertedFields)
            throws InvalidOptionConfiguration {
        final Map<String, FieldAndOption> map = new HashMap<>();

        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliOption.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw newInvalidModifierStatic(field);

            makeAccessible(field);

            final CliOption option = field.getAnnotation(CliOption.class);
            // a field converter accepts any type, only it can convert the value
            if (!convertedFields.contains(field.getName())) {
                if (!isValidFieldType(field.getType(), convertedTypes))
                    /**
                     * newInvalidOptionType.
                     * @param option the option
                     * @return the result
                     */
                    throw newInvalidOptionType(option);
                if (!isValidListType(field, convertedTypes))
                    /**
                     * newInvalidOptionType.
                     * @param option the option
                     * @return the result
                     */
                    throw newInvalidOptionType(option);
            }
            if (option.name() == ' ' && option.longName().isEmpty())
                throw newInvalidOptionName(field.getName());
            if (option.isHelp() && !isBooleanType(field))
                /**
                 * newHelpTypeNotBoolean.
                 * @param field the field
                 * @return the result
                 */
                throw newHelpTypeNotBoolean(field);
            if (option.isVersion() && !isBooleanType(field))
                /**
                 * newVersionTypeNotBoolean.
                 * @param field the field
                 * @return the result
                 */
                throw newVersionTypeNotBoolean(field);
            if (option.isVersion() && option.isHelp())
                /**
                 * newVersionAndHelp.
                 * @param field the field
                 * @return the result
                 */
                throw newVersionAndHelp(field);

            validateOptionAnnotations(field, option);

            addFieldAndOption(map, field, option);
        }

        return map;
    }

    static List<FieldAndPosition> toFieldAndPositionList(final Class<?> clazz
            , final Collection<Class<?>> convertedTypes, final Collection<String> convertedFields) {
        final List<FieldAndPosition> list = new ArrayList<>();

        boolean hasPositional = false;
        boolean hasOptionalPositional = false;

        int index = 0;
        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliPositional.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw newInvalidModifierStatic(field);
            if (isListType(field) && hasPositional) throw newConflictingPositionals();

            hasPositional = true;
            makeAccessible(field);

            final CliPositional positional = field.getAnnotation(CliPositional.class);
            if (hasOptionalPositional && FAKE_NULL.equals(positional.defaultValue()))
                /**
                 * newMandatoryPositionalAfterOptional.
                 * @return the result
                 */
                throw newMandatoryPositionalAfterOptional();

            hasOptionalPositional = hasOptionalPositional || !FAKE_NULL.equals(positional.defaultValue());
            if (isListType(field) && !FAKE_NULL.equals(positional.defaultValue()))
                /**
                 * newDefaultForListPositional.
                 * @return the result
                 */
                throw newDefaultForListPositional();
            if (!convertedFields.contains(field.getName())) {
                if (!isValidFieldType(field.getType(), convertedTypes))
                    /**
                     * newInvalidOptionType.
                     * @param field the field
                     * @return the result
                     */
                    throw newInvalidOptionType(field);
                if (!isValidListType(field, convertedTypes))
                    /**
                     * newInvalidOptionType.
                     * @param field the field
                     * @return the result
                     */
                    throw newInvalidOptionType(field);
            }

            list.add(index++, new FieldAndPosition(field, positional));
        }

        return list;
    }

    /** Fails fast on annotation values that could never work, instead of failing half way through a parse. */
    private static void validateOptionAnnotations(final Field field, final CliOption option) {
        final int arity = option.arity();
        if (arity < -1) throw newInvalidArity(field, arity);
        if ((arity > 1 || arity == -1) && !isListType(field)) throw newInvalidArity(field, arity);
        if ((option.isHelp() || option.isVersion()) && arity != 0) throw newHelpVersionArity(field);

        if (option.isCounting()) {
            if (!isCountingType(field.getType())) throw newInvalidCountingType(field);
            if (arity != 0) throw newCountingArity(field);
            if (option.isMandatory()) throw newCountingMandatory(field);
            if (!FAKE_NULL.equals(option.defaultValue())) {
                try {
                    Long.parseLong(option.defaultValue());
                } catch (final NumberFormatException e) {
                    /**
                     * newInvalidCountingDefault.
                     * @param field the field
                     * @return the result
                     */
                    throw newInvalidCountingDefault(field);
                }
            }
        }

        if (option.isInteractive() && isBooleanType(field) && arity != 1) throw newInteractiveFlag(field);

        if (!isNullOrEmpty(option.min()) || !isNullOrEmpty(option.max())) {
            final Class<?> numericType = isListType(field) ? toParameterType(field) : field.getType();
            if (!isNumericType(numericType)) throw newInvalidRange(field, option.min(), option.max());
            try {
                final BigDecimal min = isNullOrEmpty(option.min()) ? null : new BigDecimal(option.min());
                final BigDecimal max = isNullOrEmpty(option.max()) ? null : new BigDecimal(option.max());
                if (min != null && max != null && min.compareTo(max) > 0)
                    throw newInvalidRange(field, option.min(), option.max());
            } catch (final NumberFormatException e) {
                throw newInvalidRange(field, option.min(), option.max());
            }
        }

        if (!isNullOrEmpty(option.regex())) {
            try {
                Pattern.compile(option.regex());
            } catch (final PatternSyntaxException e) {
                /**
                 * newInvalidRegex.
                 * @param field the field
                 * @return the result
                 */
                throw newInvalidRegex(field);
            }
        }

        for (final String alias : option.aliases()) {
            if (isNullOrEmpty(alias)) throw newInvalidAlias(field, alias);
        }
    }

    /** The requires/excludes of an option refer to other options by name, check that those names exist. */
    private static void validateRelations(final Map<String, FieldAndOption> options) {
        final Set<CliOption> validated = new HashSet<>();
        for (final FieldAndOption fao : options.values()) {
            if (!validated.add(fao.option)) continue;
            for (final String related : fao.option.requires()) {
                if (isNullOrEmpty(related) || !options.containsKey(related))
                    throw newUnknownRelatedOption(fao.field.getName(), related);
            }
            for (final String related : fao.option.excludes()) {
                if (isNullOrEmpty(related) || !options.containsKey(related))
                    throw newUnknownRelatedOption(fao.field.getName(), related);
            }
        }
    }

    /**
     * addFieldAndOption.
     * @param map the map
     * @param field the field
     * @param option the option
     */
    private static void addFieldAndOption(final Map<String, FieldAndOption> map, final Field field, final CliOption option) {
        if (option.name() != ' ')
            addFieldAndOption(map, "" + option.name(), field, option);
        if (!option.longName().isEmpty())
            addFieldAndOption(map, option.longName(), field, option);
        for (final String alias : option.aliases())
            addFieldAndOption(map, alias, field, option);
    }

    private static void addFieldAndOption(final Map<String, FieldAndOption> map, final String name
            , final Field field, final CliOption option) {
        final FieldAndOption existing = map.get(name);
        if (existing != null && !existing.field.equals(field)) throw newDuplicateOptionName(name, field);
        map.put(name, new FieldAndOption(field, option));
    }

    private static void applyArguments(final String[] args, final Map<String, FieldAndOption> options
            , final List<FieldAndPosition> positionals, final ValueStore store, final ParseContext context) throws InvalidCommandLine, IllegalAccessException {

        initializeValues(store, options, positionals);

        final int totalNumberOfPositionalArguments = positionals.size();
        final Set<CliOption> parsedOptions = new HashSet<>();

        int positionalArgumentIndex = 1;
        boolean noMoreOptions = false;
        // Set the arguments into the object
        for (int i = 0; i < args.length; i++) {
            final String argument = args[i];
            if (!noMoreOptions && END_OF_OPTIONS.equals(argument)) {
                noMoreOptions = true;
                continue;
            }
            if (!noMoreOptions && isOption(argument, options)) {
                // an argument that looks like an option but is not one can be treated as a positional
                if (!(context.allowPrefixedPositionals && !isKnownOptionToken(argument, options))) {
                    // '-vq' and '-fdata.txt': grouped short options, the last one may take a value
                    if (isShortCluster(argument)) {
                        i += applyShortCluster(args, i, options, parsedOptions, store, context);
                        continue;
                    }
                    final String name = argumentToName(argument);
                    final FieldAndOption fao = options.get(name);
                    if (fao == null) throw context.localize(unknownArgumentError(argument, options));
                    i += applyOptionValue(args, i, argument, fao, isAttachedForm(argument), attachedValueOf(argument)
                            , parsedOptions, store, context);
                    continue;
                }
            }
            // it must be a positional if its not an option
            if (positionals.isEmpty())
                throw context.localize(newTooManyPositionalArguments(totalNumberOfPositionalArguments, positionalArgumentIndex));
            positionalArgumentIndex++;
            final FieldAndPosition fap = positionals.get(0);
            if (isListType(fap.field)) {
                final List<Object> list = (List) store.get(fap.field);
                list.add(convertPositional(context, fap, argument));
            } else {
                positionals.remove(0);
                store.set(fap.field, convertPositional(context, fap, argument));
            }
        }

        // --help and --version take precedence: never fail on missing mandatory
        // arguments or on broken requires/excludes when the user asked for help
        if (isHelpRequested(options, store)) return;

        checkRelations(options, parsedOptions, context);

        // check the left over options
        final Set<CliOption> defaulted = new HashSet<>();
        for (final FieldAndOption fao : options.values()) {
            if (!defaulted.add(fao.option)) continue;
            if (parsedOptions.contains(fao.option)) continue;
            // counting flags always have a value, they were initialized to their starting value
            if (fao.option.isCounting()) continue;

            // an explicitly configured fallback beats the annotation default
            final String external = externalDefault(context, fao);
            if (external != null) {
                setDefault(store, fao.field, context, toName(fao.option), external);
                continue;
            }
            final boolean hasDefault = fao.option.setDefaultWhenMissing()
                    && !FAKE_NULL.equals(fao.option.defaultValue());
            if (isListType(fao.field)) {
                if (hasDefault) {
                    addListDefault(store, fao, context);
                    continue;
                }
            } else if (hasDefault || isOptionalClass(fao.field.getType())) {
                // Optionals are always initialized, even without a default they become empty
                store.set(fao.field, convertValue(context, fao.field, fao.field.getType(), toName(fao.option)
                        , toRealNull(!hasDefault, fao.option.defaultValue()), fao.option));
                if (hasDefault) continue;
            }
            // only when no source could provide a value is a mandatory option missing
            if (fao.option.isMandatory() && !context.ignoreMissingMandatory)
                throw context.localize(newMissingArgument(fao.option));
        }

        // check the left over positionals
        if (positionals.size() != 1 || !isListType(positionals.get(0).field)) {
            for (final FieldAndPosition fap : positionals) {
                final String external = externalDefaultPositional(context, fap);
                if (external != null) {
                    store.set(fap.field, convertPositional(context, fap, external));
                    continue;
                }
                if (FAKE_NULL.equals(fap.position.defaultValue())) {
                    if (context.ignoreMissingMandatory) continue;
                    throw context.localize(newMissingArgument(fap.field));
                }

                store.set(fap.field, convertPositional(context, fap, fap.position.defaultValue()));
            }
        }

        return;
    }

    /**
     * addListDefault.
     * @param store the store
     * @param fao the fao
     * @param context the context
     * @throws InvalidCommandLine if an error occurs
     * @throws IllegalAccessException if an error occurs
     */
    private static void addListDefault(final ValueStore store, final FieldAndOption fao, final ParseContext context)
            throws InvalidCommandLine, IllegalAccessException {
        if (!fao.option.setDefaultWhenMissing()) return;
        final String value = fao.option.defaultValue();
        if (FAKE_NULL.equals(value)) return;
        final List<Object> list = (List) store.get(fao.field);
        for (final String element : splitListValue(value)) {
            list.add(convertValue(context, fao.field, toParameterType(fao.field), toName(fao.option), element, fao.option));
        }
    }

    private static void setDefault(final ValueStore store, final Field field, final ParseContext context
            , final String name, final String value) throws InvalidCommandLine, IllegalAccessException {
        if (isListType(field)) {
            final List<Object> list = (List) store.get(field);
            for (final String element : splitListValue(value)) {
                list.add(convertValue(context, field, toParameterType(field), name, element, null));
            }
        } else {
            store.set(field, convertValue(context, field, field.getType(), name, value, null));
        }
    }

    /** system property, then environment variable, then the configured defaults map */
    private static String externalDefault(final ParseContext context, final FieldAndOption fao) {
        if (!isNullOrEmpty(fao.option.systemProperty())) {
            final String value = System.getProperty(fao.option.systemProperty());
            if (value != null) return value;
        }
        if (!isNullOrEmpty(fao.option.environmentVariable())) {
            final String value = System.getenv(fao.option.environmentVariable());
            if (value != null) return value;
        }
        if (!fao.option.longName().isEmpty() && context.defaults.containsKey(fao.option.longName()))
            return context.defaults.get(fao.option.longName());
        if (fao.option.name() != ' ' && context.defaults.containsKey("" + fao.option.name()))
            return context.defaults.get("" + fao.option.name());
        if (context.defaults.containsKey(fao.field.getName())) return context.defaults.get(fao.field.getName());
        return null;
    }

    /**
     * externalDefaultPositional.
     * @param context the context
     * @param fap the fap
     * @return the result
     */
    private static String externalDefaultPositional(final ParseContext context, final FieldAndPosition fap) {
        final String name = toName(fap);
        if (context.defaults.containsKey(name)) return context.defaults.get(name);
        if (!name.equals(fap.field.getName()) && context.defaults.containsKey(fap.field.getName()))
            return context.defaults.get(fap.field.getName());
        return null;
    }

    /**
     * Applies a single option occurrence and returns how many extra arguments it consumed.
     * The token of the option itself is not counted, the caller always advances past it.
     * @param args the args
     * @param index the index
     * @param displayName the displayName
     * @param fao the fao
     * @param attachedForm the attachedForm
     * @param attachedValue the attachedValue
     * @param parsedOptions the parsedOptions
     * @param store the store
     * @param context the context
     * @return the result
     * @throws InvalidCommandLine if an error occurs
     * @throws IllegalAccessException if an error occurs
     */
    private static int applyOptionValue(final String[] args, final int index, final String displayName
            , final FieldAndOption fao, final boolean attachedForm, final String attachedValue
            , final Set<CliOption> parsedOptions, final ValueStore store, final ParseContext context)
            throws InvalidCommandLine, IllegalAccessException {

        final CliOption option = fao.option;
        final boolean repeatable = isListType(fao.field) || option.isCounting();
        if (!parsedOptions.add(option) && !repeatable)
            throw context.localize(newDuplicateCommandLineArgument(displayName));

        if (option.isDeprecated())
            context.deprecations.accept(context.messages.format("warning.deprecated"
                    , format("The option '%s' is deprecated", displayName), displayName));

        if (isFlagOption(fao)) {
            if (attachedForm) throw context.localize(newAttachedFormFlagArgument());
            if (option.isCounting()) incrementCounter(fao, store);
            else store.set(fao.field, TRUE);
            return 0;
        }

        final int arity = option.arity();
        final Class<?> elementType = toParameterType(fao.field);

        // '-v --files a b c': take as many values as there are
        if (isListType(fao.field) && arity == -1) {
            final List<Object> list = (List) store.get(fao.field);
            if (attachedForm) {
                list.add(convertValue(context, fao.field, elementType, displayName, attachedValue, option));
                return 0;
            }
            int consumed = 0;
            while (index + 1 + consumed < args.length && !looksLikeOption(args[index + 1 + consumed])) {
                list.add(convertValue(context, fao.field, elementType, displayName, args[index + 1 + consumed], option));
                consumed++;
            }
            if (consumed == 0) throw context.localize(missingValue(displayName, option, context));
            return consumed;
        }

        // '--pair a b': take exactly arity values
        if (isListType(fao.field) && arity > 1) {
            final List<Object> list = (List) store.get(fao.field);
            if (attachedForm) {
                final List<String> values = splitListValue(attachedValue);
                if (values.size() != arity)
                    throw context.localize(newMissingArityValues(displayName, arity, values.size()));
                for (final String value : values)
                    list.add(convertValue(context, fao.field, elementType, displayName, value, option));
                return 0;
            }
            int available = 0;
            while (available < arity && index + 1 + available < args.length
                    && !looksLikeOption(args[index + 1 + available])) available++;
            if (available < arity)
                throw context.localize(newMissingArityValues(displayName, arity, available));
            for (int k = 0; k < arity; k++)
                list.add(convertValue(context, fao.field, elementType, displayName, args[index + 1 + k], option));
            return arity;
        }

        // a single value
        final String value;
        int consumed;
        if (attachedForm) {
            value = attachedValue;
            consumed = 0;
        } else if (index + 1 < args.length && !looksLikeOption(args[index + 1])) {
            value = args[index + 1];
            consumed = 1;
        } else if (option.isInteractive()) {
            value = context.prompter.prompt(format("Enter a value for %s: ", displayName));
            if (value == null) throw context.localize(newPromptFailed(displayName));
            consumed = 0;
        } else {
            throw context.localize(missingValue(displayName, option, context));
        }

        if (option.arity() == 1 && isBooleanType(fao.field) && !isTrueOrFalse(value))
            throw context.localize(newInvalidBooleanValue(displayName, value));

        final Class<?> type = isListType(fao.field) ? elementType : fao.field.getType();
        final Object converted = convertValue(context, fao.field, type, displayName, value, option);
        if (isListType(fao.field)) ((List) store.get(fao.field)).add(converted);
        else store.set(fao.field, converted);
        return consumed;
    }

    private static InvalidCommandLine missingValue(final String displayName, final CliOption option
            , final ParseContext context) {
        return option.isInteractive()
            ? context.localize(newPromptFailed(displayName))
            : newMissingCommandLineValue(displayName);
    }

    /**
     * isTrueOrFalse.
     * @param value the value
     * @return the result
     */
    private static boolean isTrueOrFalse(final String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    /** Is this occurrence a flag, meaning it takes no value at all? */
    static boolean isFlagOption(final FieldAndOption fao) {
        return fao.option.isCounting() || (isBooleanType(fao.field) && fao.option.arity() != 1);
    }

    private static int applyShortCluster(final String[] args, final int index
            , final Map<String, FieldAndOption> options, final Set<CliOption> parsedOptions
            , final ValueStore store, final ParseContext context) throws InvalidCommandLine, IllegalAccessException {

        final String argument = args[index];
        final int equalsIndex = argument.indexOf('=');
        final String names = equalsIndex == -1 ? argument.substring(1) : argument.substring(1, equalsIndex);
        final boolean attachedForm = equalsIndex != -1;
        final String attachedValue = attachedForm ? argument.substring(equalsIndex + 1) : null;

        // '-file' is a long form typo, not a group of short options
        if (names.length() > 1 && options.containsKey(names))
            throw context.localize(newSingleDashAttachedFormArgument(argument).suggest("--" + names));

        int consumed = 0;
        for (int position = 0; position < names.length(); position++) {
            final String name = String.valueOf(names.charAt(position));
            final FieldAndOption fao = options.get(name);
            if (fao == null) throw context.localize(unknownArgumentError(toDisplayName(name), options, argument));

            final String displayName = toDisplayName(name);
            if (!isFlagOption(fao)) {
                // the rest of the token is the value: '-vfdata.txt', otherwise the next token is
                final String rest = names.substring(position + 1);
                final boolean hasRest = !rest.isEmpty();
                return applyOptionValue(args, index, displayName, fao, hasRest || attachedForm
                        , hasRest ? rest : attachedValue, parsedOptions, store, context);
            }
            if (position == names.length() - 1 && attachedForm)
                throw context.localize(newAttachedFormFlagArgument());

            applyOptionValue(args, index, displayName, fao, false, null, parsedOptions, store, context);
        }
        return consumed;
    }

    /** Seeds the values that parsing accumulates into: a fresh list per List field, the start of every counter. */
    private static void initializeValues(final ValueStore store, final Map<String, FieldAndOption> options
            , final List<FieldAndPosition> positionals) throws IllegalAccessException {
        final Set<Field> seeded = new HashSet<>();
        for (final FieldAndOption fao : options.values()) {
            if (!seeded.add(fao.field)) continue;
            if (isListType(fao.field)) {
                store.set(fao.field, new ArrayList<>());
            } else if (fao.option.isCounting()) {
                final long start = FAKE_NULL.equals(fao.option.defaultValue())
                    ? 0L : Long.parseLong(fao.option.defaultValue());
                if (isLong(fao.field)) store.set(fao.field, start);
                else store.set(fao.field, (int) start);
            }
        }
        for (final FieldAndPosition fap : positionals) {
            if (isListType(fap.field) && seeded.add(fap.field)) store.set(fap.field, new ArrayList<>());
        }
    }

    /**
     * incrementCounter.
     * @param fao the fao
     * @param store the store
     * @throws IllegalAccessException if an error occurs
     */
    private static void incrementCounter(final FieldAndOption fao, final ValueStore store) throws IllegalAccessException {
        final Object current = store.get(fao.field);
        if (isLong(fao.field)) store.set(fao.field, ((Number) current).longValue() + 1L);
        else store.set(fao.field, ((Number) current).intValue() + 1);
    }

    /**
     * isLong.
     * @param field the field
     * @return the result
     */
    private static boolean isLong(final Field field) {
        return Long.class.equals(field.getType()) || long.class.equals(field.getType());
    }

    /**
     * isHelpRequested.
     * @param options the options
     * @param store the store
     * @return the result
     */
    private static boolean isHelpRequested(final Map<String, FieldAndOption> options, final ValueStore store) {
        final Set<CliOption> checked = new HashSet<>();
        for (final FieldAndOption fao : options.values()) {
            if (!checked.add(fao.option)) continue;
            if (!fao.option.isHelp() && !fao.option.isVersion()) continue;
            try {
                if (TRUE.equals(store.get(fao.field))) return true;
            } catch (final IllegalAccessException e) {
                return false;
            }
        }
        return false;
    }

    /** Options that declare requires/excludes may only be combined as configured. */
    private static void checkRelations(final Map<String, FieldAndOption> options, final Set<CliOption> parsedOptions
            , final ParseContext context) throws InvalidCommandLine {
        final Set<CliOption> checked = new HashSet<>();
        for (final FieldAndOption fao : options.values()) {
            if (!checked.add(fao.option)) continue;
            if (!parsedOptions.contains(fao.option)) continue;
            final String name = toName(fao.option);
            for (final String required : fao.option.requires()) {
                final FieldAndOption other = options.get(required);
                if (other == null || !parsedOptions.contains(other.option))
                    throw context.localize(newMissingRequiredArgument(name, toDisplayName(required)));
            }
            for (final String excluded : fao.option.excludes()) {
                final FieldAndOption other = options.get(excluded);
                if (other != null && parsedOptions.contains(other.option))
                    throw context.localize(newExcludedArgument(name, toDisplayName(excluded)));
            }
        }
    }

    /**
     * convertPositional.
     * @param context the context
     * @param fap the fap
     * @param value the value
     * @return the result
     * @throws InvalidCommandLine if an error occurs
     */
    private static Object convertPositional(final ParseContext context, final FieldAndPosition fap, final String value)
            throws InvalidCommandLine {
        final Class<?> type = isListType(fap.field) ? toParameterType(fap.field) : fap.field.getType();
        return convertValue(context, fap.field, type, toName(fap), value, null);
    }

    /** Converts a value and validates it, errors always name the option and the type that was expected. */
    private static Object convertValue(final ParseContext context, final Field field, final Class<?> type
            , final String name, final String value, final CliOption option) throws InvalidCommandLine {
        final Object converted;
        try {
            converted = context.convert.toFieldType(field, type, value);
        } catch (final InvalidCommandLine | RuntimeException e) {
            // includes custom converters that blow up on the value, like NumberFormatException
            final InvalidCommandLine error = newInvalidValue(name, value, type.getSimpleName());
            error.initCause(e);
            throw context.localize(error);
        }
        validateValue(context, option, name, value, converted);
        return converted;
    }

    private static void validateValue(final ParseContext context, final CliOption option, final String name
            , final String value, final Object converted) throws InvalidCommandLine {
        if (option == null) return;
        if (option.allowedValues().length > 0 && !Arrays.asList(option.allowedValues()).contains(value))
            throw context.localize(newDisallowedValue(name, value, join(", ", option.allowedValues())));
        if (!isNullOrEmpty(option.regex()) && !value.matches(option.regex()))
            throw context.localize(newInvalidRegexValue(name, value, option.regex()));
        if (isNullOrEmpty(option.min()) && isNullOrEmpty(option.max())) return;
        if (!(converted instanceof Number)) return;
        final BigDecimal number = new BigDecimal(converted.toString());
        if (!isNullOrEmpty(option.min()) && number.compareTo(new BigDecimal(option.min())) < 0)
            throw context.localize(newValueTooSmall(name, value, option.min()));
        if (!isNullOrEmpty(option.max()) && number.compareTo(new BigDecimal(option.max())) > 0)
            throw context.localize(newValueTooLarge(name, value, option.max()));
    }

    /**
     * unknownArgumentError.
     * @param token the token
     * @param options the options
     * @return the result
     */
    private static InvalidCommandLine unknownArgumentError(final String token, final Map<String, FieldAndOption> options) {
        final String typed = token.startsWith("--") ? token.substring(2) : token.substring(1);
        final String suggestion = closestMatch(typed, options.keySet());
        final InvalidCommandLine error = newUnknownCommandLineArgument(token);
        if (suggestion != null) error.suggest(toDisplayName(suggestion));
        return error;
    }

    /** For '-file' where '--file' exists we can do better than a generic unknown option error. */
    private static InvalidCommandLine unknownArgumentError(final String token, final Map<String, FieldAndOption> options
            , final String wholeToken) {
        if (wholeToken.length() > 2 && options.containsKey(wholeToken.substring(1)))
            return newSingleDashAttachedFormArgument(wholeToken).suggest("--" + wholeToken.substring(1));
        return unknownArgumentError(token, options);
    }

    /**
     * Finds the first token that is neither an option nor the value of an option.
     * That is where a subcommand starts. Returns -1 when the command line has no subcommand.
     * @param args the args
     * @param options the options
     * @return the result
     */
    static int findSubcommandIndex(final String[] args, final Map<String, FieldAndOption> options) {
        boolean noMoreOptions = false;
        for (int i = 0; i < args.length; i++) {
            final String argument = args[i];
            if (noMoreOptions) return i;
            if (END_OF_OPTIONS.equals(argument)) {
                noMoreOptions = true;
                continue;
            }
            if (!isOption(argument, options)) return i;

            if (isShortCluster(argument)) {
                final int consumed = consumedByCluster(argument, options);
                // a broken cluster is an error for the parser to report, not a subcommand
                if (consumed < 0) return -1;
                i += consumed;
                continue;
            }
            final FieldAndOption fao = options.get(safeName(argument));
            if (fao == null) {
                // an unknown option: assume it takes a value so it does not swallow the subcommand
                if (!isAttachedForm(argument) && i + 1 < args.length) i++;
                continue;
            }
            if (isFlagOption(fao)) continue;
            if (!isAttachedForm(argument) && i + 1 < args.length) i++;
        }
        return -1;
    }

    /** How many arguments a group of short options consumes after its own token. -1 when the group is invalid. */
    private static int consumedByCluster(final String argument, final Map<String, FieldAndOption> options) {
        final int equalsIndex = argument.indexOf('=');
        final String names = equalsIndex == -1 ? argument.substring(1) : argument.substring(1, equalsIndex);
        for (int position = 0; position < names.length(); position++) {
            final FieldAndOption fao = options.get(String.valueOf(names.charAt(position)));
            if (fao == null) return -1;
            if (isFlagOption(fao)) continue;
            // this option takes the rest of the token, or the next token when it is last without a value
            if (position < names.length() - 1 || equalsIndex != -1) return 0;
            return 1;
        }
        return 0;
    }

    /**
     * safeName.
     * @param argument the argument
     * @return the result
     */
    private static String safeName(final String argument) {
        try {
            return argumentToName(argument);
        } catch (final InvalidCommandLine e) {
            return null;
        }
    }

    /**
     * toRealNull.
     * @param mustBeNull the mustBeNull
     * @param value the value
     * @return the result
     */
    private static String toRealNull(final boolean mustBeNull, final String value) {
        return mustBeNull || FAKE_NULL.equals(value) ? null : value;
    }

    /**
     * isOptionalClass.
     * @param clazz the clazz
     * @return the result
     */
    private static boolean isOptionalClass(final Class<?> clazz) {
        return clazz.equals(OptionalLong.class)
            || clazz.equals(OptionalInt.class)
            || clazz.equals(OptionalDouble.class)
            || clazz.equals(Optional.class);
    }

    /**
     * isOption.
     * @param argument the argument
     * @param options the options
     * @return the result
     */
    private static boolean isOption(final String argument, final Map<String, FieldAndOption> options) {
        if (!isOption(argument)) return false;
        if (!isNegativeNumber(argument)) return true;
        // A negative number is only an option when a field actually declared that short name
        return argument.length() == 2 && options.containsKey(argument.substring(1));
    }

    /**
     * isOption.
     * @param argument the argument
     * @return the result
     */
    private static boolean isOption(final String argument) {
        return !argument.isEmpty() && argument.charAt(0) == '-';
    }

    /**
     * isNegativeNumber.
     * @param argument the argument
     * @return the result
     */
    private static boolean isNegativeNumber(final String argument) {
        return NEGATIVE_NUMBER.matcher(argument).matches();
    }

    // Values that look like another option are not values at all, negative numbers are the exception
    /**
     * looksLikeOption.
     * @param argument the argument
     * @return the result
     */
    private static boolean looksLikeOption(final String argument) {
        return isOption(argument) && !isNegativeNumber(argument);
    }

    // '-vq' or '-fdata.txt': a single dash with more than one character
    /**
     * isShortCluster.
     * @param argument the argument
     * @return the result
     */
    private static boolean isShortCluster(final String argument) {
        return argument.length() > 2 && argument.charAt(0) == '-' && !argument.startsWith("--");
    }

    /** Does every option name in this token exist? Used by the prefixed positional fallback. */
    private static boolean isKnownOptionToken(final String argument, final Map<String, FieldAndOption> options) {
        if (isShortCluster(argument)) {
            final int equalsIndex = argument.indexOf('=');
            final String names = equalsIndex == -1 ? argument.substring(1) : argument.substring(1, equalsIndex);
            for (int position = 0; position < names.length(); position++) {
                if (!options.containsKey(String.valueOf(names.charAt(position)))) return false;
            }
            return true;
        }
        final String name = safeName(argument);
        return name != null && options.containsKey(name);
    }

    /**
     * isAttachedForm.
     * @param argument the argument
     * @return the result
     */
    private static boolean isAttachedForm(final String argument) {
        return argument.contains("=");
    }

    /**
     * attachedValueOf.
     * @param argument the argument
     * @return the result
     */
    private static String attachedValueOf(final String argument) {
        final int equalsIndex = argument.indexOf('=');
        return equalsIndex == -1 ? null : argument.substring(equalsIndex + 1);
    }

    /**
     * argumentToName.
     * @param argument the argument
     * @return the result
     * @throws InvalidCommandLine if an error occurs
     */
    private static String argumentToName(final String argument) throws InvalidCommandLine {
        if (argument.charAt(0) != '-') throw newInvalidCommandLineArgument(argument);
        if (argument.startsWith("--")) {
            if (argument.length() == 2) throw newCommandLineArgumentTooShort();
            final int equalsIndex = argument.indexOf('=');
            return equalsIndex == -1 ? argument.substring(2) : argument.substring(2, equalsIndex);
        }

        if (argument.length() == 1) throw newCommandLineArgumentTooShort();
        final int equalsIndex = argument.indexOf('=');
        if (equalsIndex == -1) {
            if (argument.length() > 2) throw newSingleDashAttachedFormArgument(argument);
            return argument.substring(1);
        }
        if (equalsIndex == 1) throw newInvalidCommandLineArgument(argument);
        if (equalsIndex > 2) throw newSingleDashAttachedFormArgument(argument);
        return argument.substring(1, 2);
    }

    /**
     * newNoArgInstance.
     * @param clazz the clazz
     * @return the result
     * @param <T> the type parameter
     */
    private static <T> T newNoArgInstance(final Class<T> clazz) {
        try {
            final Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final NoSuchMethodException | InstantiationException e) {
            /**
             * newMissingNoArgConstructor.
             * @param clazz the clazz
             * @return the result
             */
            throw newMissingNoArgConstructor(clazz);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getTargetException();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            /**
             * RuntimeException.
             * @param cause the cause
             * @return the result
             */
            throw new RuntimeException(cause);
        } catch (final ReflectiveOperationException e) {
            /**
             * RuntimeException.
             * @param e the e
             * @return the result
             */
            throw new RuntimeException(e);
        }
    }

    /**
     * Where the parser writes the values it converts: directly into a ready made arguments
     * instance, or staged so that a record can be built through its canonical constructor.
     * A record cannot be modified after construction, so every write is held until build().
     */
    private static final class ValueStore {

        private final Object instance;
        private final Class<?> recordClass;
        private final Constructor<?> recordConstructor;
        private final Map<String, Object> staged;

        /**
         * ValueStore.
         * @param instance the instance
         * @param recordClass the recordClass
         * @param recordConstructor the recordConstructor
         * @return the result
         */
        private ValueStore(final Object instance, final Class<?> recordClass, final Constructor<?> recordConstructor) {
            this.instance = instance;
            this.recordClass = recordClass;
            this.recordConstructor = recordConstructor;
            this.staged = recordClass == null ? null : new HashMap<>();
        }

        /** A store that writes every value into the given instance. */
        public static ValueStore forInstance(final Object instance) {
            return new ValueStore(instance, null, null);
        }

        /**
         * A store that stages values for a record: the record does not exist yet, so values are
         * collected until build() calls the canonical constructor. Any component that nothing
         * wrote gets the default for its type, exactly like an untouched field of a class.
         * @param clazz the clazz
         * @return the result
         */
        public static ValueStore forClass(final Class<?> clazz) {
            if (!clazz.isRecord()) return new ValueStore(newNoArgInstance(clazz), null, null);
            try {
                final Class<?>[] parameterTypes = Arrays.stream(clazz.getRecordComponents())
                        .map(RecordComponent::getType).toArray(Class<?>[]::new);
                final Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
                return new ValueStore(null, clazz, constructor);
            } catch (final ReflectiveOperationException e) {
                /**
                 * RuntimeException.
                 * @param e the e
                 * @return the result
                 */
                throw new RuntimeException(e);
            }
        }

        /** Reads the current value: from the instance, or what was staged for this field so far. */
        public Object get(final Field field) throws IllegalAccessException {
            return recordClass == null ? field.get(instance) : staged.get(field.getName());
        }

        /** Writes a converted value: into the instance, or into the staging map of a record. */
        public void set(final Field field, final Object value) throws IllegalAccessException {
            if (recordClass == null) field.set(instance, value);
            else staged.put(field.getName(), value);
        }

        /** Returns the instance, or calls the canonical constructor of a record with the staged values. */
        public Object build() {
            if (recordClass == null) return instance;
            final RecordComponent[] components = recordClass.getRecordComponents();
            final Object[] values = new Object[components.length];
            for (int i = 0; i < values.length; i++) values[i] = valueOrDefault(components[i]);
            try {
                return recordConstructor.newInstance(values);
            } catch (final InvocationTargetException e) {
                // a compact constructor rejected the values: that error is the record's own, pass it on
                final Throwable cause = e.getTargetException();
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                if (cause instanceof Error) throw (Error) cause;
                /**
                 * RuntimeException.
                 * @param cause the cause
                 * @return the result
                 */
                throw new RuntimeException(cause);
            } catch (final ReflectiveOperationException e) {
                /**
                 * RuntimeException.
                 * @param e the e
                 * @return the result
                 */
                throw new RuntimeException(e);
            }
        }

        /**
         * valueOrDefault.
         * @param component the component
         * @return the result
         */
        private Object valueOrDefault(final RecordComponent component) {
            final Object value = staged.get(component.getName());
            if (value != null) return value;
            final Class<?> type = component.getType();
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == char.class) return (char) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0F;
            if (type == double.class) return 0D;
            return null;
        }
    }

}
