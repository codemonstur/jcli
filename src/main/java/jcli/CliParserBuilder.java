package jcli;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import jcli.model.FieldAndOption;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static jcli.CliHelp.getHelp;
import static jcli.CliParser.parseCommandLineArguments;
import static jcli.Reflection.*;
import static jcli.Util.isNullOrEmpty;
import static jcli.errors.InvalidCommandLine.newUnknownSubcommand;
import static jcli.errors.InvalidOptionConfiguration.*;

/**
 * The CliParserBuilder class.
 * @param <T> the type parameter
 */
public class CliParserBuilder<T> {

    /** Creates a new builder. */
    public CliParserBuilder() {
    }

    /**
     * The CliErrorConsumer interface.
     */
    public interface CliErrorConsumer {
        /**
         * cliArgumentsInvalid.
         * @param e the e
         * @param args the args
         */
        void cliArgumentsInvalid(InvalidCommandLine e, String[] args);
    }
    /**
     * The CliHelpConsumer interface.
     * @param <A> the type parameter
     */
    public interface CliHelpConsumer<A> {
        /**
         * cliHelpRequested.
         * @param arguments the arguments
         * @param args the args
         */
        void cliHelpRequested(A arguments, String[] args);
    }
    /**
     * The CliVersionConsumer interface.
     */
    public interface CliVersionConsumer {
        /**
         * printVersion.
         * @param vendor the vendor
         * @param name the name
         * @param version the version
         */
        void printVersion(String vendor, String name, String version);
    }

    /**
     * newCliParser.
     * @param <U> the type parameter
     * @param supplier the supplier
     * @return the result
     */
    public static <U> CliParserBuilder<U> newCliParser(final Supplier<U> supplier) {
        return new CliParserBuilder<U>().object(supplier);
    }

    /**
     * Starts a parser for an arguments class: a record, or a class with a no-arg constructor.
      * @param <U> the type parameter
      * @param argumentsClass the argumentsClass
      * @return the result
     */
    public static <U> CliParserBuilder<U> newCliParser(final Class<U> argumentsClass) {
        return new CliParserBuilder<U>().object(argumentsClass);
    }
    /**
     * newCliParser.
     * @param <U> the type parameter
     * @return the result
     */
    public static <U> CliParserBuilder<U> newCliParser() {
        return new CliParserBuilder<>();
    }

    static final class Subcommand {
        final String description;
        final CliParserBuilder<?> parser;
        Subcommand(final String description, final CliParserBuilder<?> parser) {
            this.description = description;
            this.parser = parser;
        }
    }

    private String name;
    private Supplier<T> supplier;
    private Class<T> argumentsClass;
    private boolean onErrorPrintHelpAndExit = false;
    private boolean onHelpPrintHelpAndExit = false;
    private boolean onVersionPrintVersionAndExit = false;
    private int helpExitCode = 1;
    private int errorExitCode = 2;
    private int versionExitCode = 3;
    private CliErrorConsumer onErrorCall;
    private CliHelpConsumer<T> onHelpCall;
    private CliVersionConsumer onVersionCall;
    private Consumer<String> helpConsumer = System.out::println;
    private Consumer<String> errorConsumer = System.err::println;
    private CliVersionConsumer versionConsumer = (vendor, name, version) ->
        System.out.println((name == null ? "unknown" : name) + (version == null ? "" : " v" + version));
    private String helpIndent;
    private int helpWidth = 0;
    private Map<Class<?>, StringToType<?>> classConverters = new HashMap<>();
    private Map<String, StringToType<?>> fieldConverters = new HashMap<>();
    private Messages messages = Messages.DEFAULT;
    private CliPrompter prompter = new ConsolePrompter();
    private boolean ignoreMissingMandatory = false;
    private boolean allowPrefixedPositionals = false;
    private Map<String, String> defaults = Collections.emptyMap();
    private Consumer<String> deprecationConsumer = System.err::println;
    private IntConsumer exitHandler = System::exit;
    private final Map<String, Subcommand> subcommands = new LinkedHashMap<>();

    /**
     * name.
     * @param name the name
     * @return the result
     */
    public CliParserBuilder<T> name(final String name) {
        this.name = name;
        return this;
    }
    /**
     * object.
     * @param supplier the supplier
     * @return the result
     */
    public CliParserBuilder<T> object(final Supplier<T> supplier) {
        this.supplier = supplier;
        this.argumentsClass = null;
        return this;
    }

    /**
     * Uses an arguments class instead of a Supplier: a record is built through its canonical constructor.
      * @param argumentsClass the argumentsClass
      * @return the result
     */
    public CliParserBuilder<T> object(final Class<T> argumentsClass) {
        this.argumentsClass = argumentsClass;
        this.supplier = null;
        return this;
    }
    /**
     * onErrorPrintHelpAndExit.
     * @return the result
     */
    public CliParserBuilder<T> onErrorPrintHelpAndExit() {
        onErrorPrintHelpAndExit = true;
        return this;
    }
    /**
     * onErrorCall.
     * @param consumer the consumer
     * @return the result
     */
    public CliParserBuilder<T> onErrorCall(final CliErrorConsumer consumer) {
        onErrorCall = consumer;
        return this;
    }
    /**
     * errorExitCode.
     * @param code the code
     * @return the result
     */
    public CliParserBuilder<T> errorExitCode(final int code) {
        this.errorExitCode = code;
        return this;
    }
    /**
     * onHelpPrintHelpAndExit.
     * @return the result
     */
    public CliParserBuilder<T> onHelpPrintHelpAndExit() {
        onHelpPrintHelpAndExit = true;
        return this;
    }
    /**
     * onHelpCall.
     * @param consumer the consumer
     * @return the result
     */
    public CliParserBuilder<T> onHelpCall(final CliHelpConsumer<T> consumer) {
        onHelpCall = consumer;
        return this;
    }
    /**
     * helpExitCode.
     * @param code the code
     * @return the result
     */
    public CliParserBuilder<T> helpExitCode(final int code) {
        this.helpExitCode = code;
        return this;
    }
    /**
     * helpConsumer.
     * @param helpConsumer the helpConsumer
     * @return the result
     */
    public CliParserBuilder<T> helpConsumer(final Consumer<String> helpConsumer) {
        this.helpConsumer = helpConsumer;
        return this;
    }

    /**
     * Where the '[ERROR] message' line of onErrorPrintHelpAndExit goes. Defaults to System.err.
      * @param errorConsumer the errorConsumer
      * @return the result
     */
    public CliParserBuilder<T> errorConsumer(final Consumer<String> errorConsumer) {
        this.errorConsumer = errorConsumer;
        return this;
    }


    /**
     * onVersionPrintVersionAndExit.
     * @return the result
     */
    public CliParserBuilder<T> onVersionPrintVersionAndExit() {
        onVersionPrintVersionAndExit = true;
        return this;
    }
    /**
     * onVersionCall.
     * @param onVersionCall the onVersionCall
     * @return the result
     */
    public CliParserBuilder<T> onVersionCall(final CliVersionConsumer onVersionCall) {
        this.onVersionCall = onVersionCall;
        return this;
    }
    /**
     * versionExitCode.
     * @param code the code
     * @return the result
     */
    public CliParserBuilder<T> versionExitCode(final int code) {
        this.versionExitCode = code;
        return this;
    }
    /**
     * versionConsumer.
     * @param versionConsumer the versionConsumer
     * @return the result
     */
    public CliParserBuilder<T> versionConsumer(final CliVersionConsumer versionConsumer) {
        this.versionConsumer = versionConsumer;
        return this;
    }

    /**
     * Replaces System.exit, mostly useful in tests and when embedding the parser.
     * When the handler returns, the help and version paths continue normally and the error path rethrows.
      * @param exitHandler the exitHandler
      * @return the result
     */
    public CliParserBuilder<T> exitHandler(final IntConsumer exitHandler) {
        this.exitHandler = exitHandler;
        return this;
    }

    /**
     * indent.
     * @param indent the indent
     * @return the result
     */
    public CliParserBuilder<T> indent(final String indent) {
        this.helpIndent = indent;
        return this;
    }

    /**
     * Wraps the descriptions in the help output. Zero, the default, means no wrapping.
      * @param width the width
      * @return the result
     */
    public CliParserBuilder<T> helpWidth(final int width) {
        this.helpWidth = width;
        return this;
    }

    /**
     * Translates the built in help and error text. Keys that are missing fall back to English.
      * @param bundle the bundle
      * @return the result
     */
    public CliParserBuilder<T> resourceBundle(final ResourceBundle bundle) {
        this.messages = new Messages(bundle);
        return this;
    }

    /**
     * Supplies the value of options that are annotated with isInteractive.
      * @param prompter the prompter
      * @return the result
     */
    public CliParserBuilder<T> prompter(final CliPrompter prompter) {
        this.prompter = prompter;
        return this;
    }

    /**
     * Missing mandatory options and positionals are no longer an error.
      * @return the result
     */
    public CliParserBuilder<T> ignoreMissingMandatory() {
        this.ignoreMissingMandatory = true;
        return this;
    }

    /**
     * Arguments that look like an option but are not one are treated as positional arguments.
      * @return the result
     */
    public CliParserBuilder<T> allowPrefixedPositionals() {
        this.allowPrefixedPositionals = true;
        return this;
    }

    /**
     * Values for options that are not on the command line, looked up by long name, short name or field name.
     * They beat the defaultValue of the annotation.
      * @param map the map
      * @return the result
     */
    public CliParserBuilder<T> defaultsFrom(final Map<?, ?> map) {
        this.defaults = ParseContext.toDefaultsMap(map);
        return this;
    }
    /**
     * defaultsFrom.
     * @param bundle the bundle
     * @return the result
     */
    public CliParserBuilder<T> defaultsFrom(final ResourceBundle bundle) {
        final Map<String, String> values = new HashMap<>();
        final Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            values.put(key, bundle.getString(key));
        }
        this.defaults = values;
        return this;
    }

    /**
     * Called when a deprecated option is used. Defaults to printing a warning on stderr.
      * @param consumer the consumer
      * @return the result
     */
    public CliParserBuilder<T> deprecationConsumer(final Consumer<String> consumer) {
        this.deprecationConsumer = consumer;
        return this;
    }

    /**
     * addClassConverter.
     * @param <C> the type parameter
     * @param clazz the clazz
     * @param converter the converter
     * @return the result
     */
    public <C> CliParserBuilder<T> addClassConverter(final Class<C> clazz, final StringToType<C> converter) {
        this.classConverters.put(clazz, converter);
        return this;
    }

    /**
     * A converter for one specific field, looked up by field name.
     * It beats the built in conversion and the class converters, and lets a field hold a type
     * that nothing else can convert.
      * @param <D> the type parameter
      * @param fieldName the fieldName
      * @param converter the converter
     * @return the result
     */
    public <D> CliParserBuilder<T> fieldConverter(final String fieldName, final StringToType<D> converter) {
        this.fieldConverters.put(fieldName, converter);
        return this;
    }

    /**
     * Registers a subcommand: the first argument that is not an option selects it.
      * @param name the name
      * @param parser the parser
      * @return the result
     */
    public CliParserBuilder<T> subcommand(final String name, final CliParserBuilder<?> parser) {
        return subcommand(name, "", parser);
    }
    /**
     * subcommand.
     * @param name the name
     * @param description the description
     * @param parser the parser
     * @return the result
     */
    public CliParserBuilder<T> subcommand(final String name, final String description, final CliParserBuilder<?> parser) {
        if (isNullOrEmpty(name) || name.startsWith("-"))
            /**
             * newInvalidSubcommandName.
             * @param name the name
             * @return the result
             */
            throw newInvalidSubcommandName(name);
        if (parser == null)
            /**
             * newMissingSubcommandParser.
             * @param name the name
             * @return the result
             */
            throw newMissingSubcommandParser(name);
        if (subcommands.containsKey(name))
            /**
             * newDuplicateSubcommand.
             * @param name the name
             * @return the result
             */
            throw newDuplicateSubcommand(name);
        subcommands.put(name, new Subcommand(description == null ? "" : description, parser));
        return this;
    }

    /**
     * getSubcommandDescriptions.
     * @return the result
     */
    public Map<String, String> getSubcommandDescriptions() {
        final Map<String, String> descriptions = new LinkedHashMap<>();
        for (final Map.Entry<String, Subcommand> entry : subcommands.entrySet())
            descriptions.put(entry.getKey(), entry.getValue().description);
        return descriptions;
    }

    /**
     * The help text with all the settings of this builder applied:
     * name, indent, width, resource bundle and subcommand descriptions.
      * @return the result
     */
    public String getHelpText() {
        return getHelp(name, helpIndent, getArgumentsClass(), classConverters.keySet()
                , fieldConverters.keySet(), messages, helpWidth, getSubcommandDescriptions());
    }

    /**
     * getName.
     * @return the result
     */
    String getName() {
        return name;
    }

    Class<?> getArgumentsClass() {
        return argumentsClass != null ? argumentsClass : supplier.get().getClass();
    }

    /**
     * getSubcommands.
     * @return the result
     */
    Map<String, Subcommand> getSubcommands() {
        return subcommands;
    }

    // Not pretty but so far the only solution to the checked Exception problem
    /**
     * parseSuppressErrors.
     * @param args the args
     * @return the result
     */
    public T parseSuppressErrors(final String[] args) {
        try {
            return parse(args);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * parse.
     * @param args the args
     * @return the result
     * @throws InvalidCommandLine if an error occurs
     */
    public T parse(final String[] args) throws InvalidCommandLine {
        if (!subcommands.isEmpty())
            /**
             * newInvalidSubcommandConfiguration.
             * @return the result
             */
            throw newInvalidSubcommandConfiguration();
        if (argumentsClass != null)
            return parseWithSettings(args, context -> parseCommandLineArguments(args, argumentsClass, context));
        return parseWithSettings(args, context -> parseCommandLineArguments(args, supplier.get(), context));
    }

    /**
     * Parses a command line that can contain subcommands.
     * The result holds the arguments of the root command and of every subcommand that was selected.
      * @param args the args
      * @return the result
      * @throws jcli.errors.InvalidCommandLine if an error occurs
     * @throws InvalidCommandLine if an error occurs
     */
    public CommandResult<T> parseWithSubcommands(final String[] args) throws InvalidCommandLine {
        @SuppressWarnings("unchecked")
        final CommandResult<T> result = (CommandResult<T>) parseInternal(args);
        return result;
    }

    private CommandResult<?> parseInternal(final String[] args) throws InvalidCommandLine {
        final Object object = argumentsClass == null ? supplier.get() : null;
        final Class<?> rootClass = argumentsClass != null ? argumentsClass : object.getClass();
        final List<Object> chain = new ArrayList<>();
        final List<String> path = new ArrayList<>();

        int index = -1;
        if (!subcommands.isEmpty()) {
            if (!CliParser.toFieldAndPositionList(rootClass, classConverters.keySet()
                    , fieldConverters.keySet()).isEmpty())
                throw newSubcommandWithPositionals(isNullOrEmpty(name) ? rootClass.getSimpleName() : name);
            final Map<String, FieldAndOption> options = CliParser.toFieldAndOptionMap(rootClass
                    , classConverters.keySet(), fieldConverters.keySet());
            index = CliParser.findSubcommandIndex(args, options);
        }

        final String[] rootArgs = index == -1 ? args : Arrays.copyOfRange(args, 0, index);
        if (object != null)
            chain.add(parseWithSettings(rootArgs, context -> parseCommandLineArguments(rootArgs, object, context)));
        else
            chain.add(parseWithSettings(rootArgs, context -> parseCommandLineArguments(rootArgs, argumentsClass, context)));

        if (index != -1) {
            final String subcommandName = args[index];
            final Subcommand subcommand = subcommands.get(subcommandName);
            if (subcommand == null) {
                final String suggestion = Util.closestMatch(subcommandName, subcommands.keySet());
                final InvalidCommandLine error = newUnknownSubcommand(subcommandName);
                if (suggestion != null) error.suggest(suggestion);
                throw error;
            }
            path.add(subcommandName);
            final CommandResult<?> child = subcommand.parser
                    .parseInternal(Arrays.copyOfRange(args, index + 1, args.length));
            chain.addAll(child.getArgumentsChain());
            path.addAll(child.getPath());
        }

        return new CommandResult<>(chain, path);
    }

    /** Runs the parse that was prepared for the arguments: an instance or a class. */
    private interface DeferredParse<A> {
        /**
         * parse.
         * @param context the context
         * @return the result
         * @throws InvalidCommandLine if an error occurs
         */
        A parse(ParseContext context) throws InvalidCommandLine;
    }

    /**
     * parseWithSettings.
     * @param args the args
     * @param parse the parse
     * @return the result
     * @throws InvalidCommandLine if an error occurs
     * @param <A> the type parameter
     */
    private <A> A parseWithSettings(final String[] args, final DeferredParse<A> parse) throws InvalidCommandLine {
        try {
            final A arguments = parse.parse(parseContext());
            if (isVersionSelected(arguments)) {
                final Package currentPackage = CliParserBuilder.class.getPackage();
                final String vendor = currentPackage.getImplementationVendor();
                final String implementationName = currentPackage.getImplementationTitle();
                final String version = currentPackage.getImplementationVersion();
                final String title = implementationName == null ? name : implementationName;
                if (onVersionCall != null) onVersionCall.printVersion(vendor, title, version);
                if (onVersionPrintVersionAndExit) {
                    versionConsumer.printVersion(vendor, title, version);
                    exitHandler.accept(versionExitCode);
                }
            }
            if (isHelpSelected(arguments)) {
                if (onHelpCall != null) onHelpCall.cliHelpRequested((T) arguments, args);
                if (onHelpPrintHelpAndExit) {
                    helpConsumer.accept(getHelpText());
                    exitHandler.accept(helpExitCode);
                }
            }
            return arguments;
        } catch (final InvalidCommandLine e) {
            if (onErrorCall != null) onErrorCall.cliArgumentsInvalid(e, args);
            if (onErrorPrintHelpAndExit) {
                errorConsumer.accept("[ERROR] " + e.getMessage() + "\n");
                helpConsumer.accept(getHelpText());
                exitHandler.accept(errorExitCode);
            }
            throw e;
        }
    }

    /**
     * parseContext.
     * @return the result
     */
    private ParseContext parseContext() {
        return new ParseContext(classConverters.keySet(), messages, prompter, newFieldToType()
                , ignoreMissingMandatory, allowPrefixedPositionals, defaults, fieldConverters, deprecationConsumer);
    }

    /**
     * newFieldToType.
     * @return the result
     */
    private ToFieldType newFieldToType() {
        return (field, type, value) -> {
            // a converter registered for this very field wins over everything else
            final StringToType<?> fieldConverter = fieldConverters.get(field.getName());
            if (fieldConverter != null) return fieldConverter.toType(value);
            for (final Map.Entry<Class<?>, StringToType<?>> entry : classConverters.entrySet()) {
                if (type.isAssignableFrom(entry.getKey())) return entry.getValue().toType(value);
            }
            return toFieldType(field, type, value);
        };
    }

    /**
     * isVersionSelected.
     * @param arguments the arguments
     * @return the result
     * @param <A> the type parameter
     */
    private <A> boolean isVersionSelected(final A arguments) {
        return hasFlagSet(arguments, CliOption::isVersion);
    }

    /**
     * isHelpSelected.
     * @param arguments the arguments
     * @return the result
     * @param <A> the type parameter
     */
    private <A> boolean isHelpSelected(final A arguments) {
        return hasFlagSet(arguments, CliOption::isHelp);
    }

    /**
     * hasFlagSet.
     * @param arguments the arguments
     * @param selector the selector
     * @return the result
     * @param <A> the type parameter
     */
    private <A> boolean hasFlagSet(final A arguments, final Function<CliOption, Boolean> selector) {
        try {
            for (final Field field : listFields(arguments.getClass())) {
                if (!field.isAnnotationPresent(CliOption.class)) continue;
                makeAccessible(field);
                final CliOption option = field.getAnnotation(CliOption.class);
                if (selector.apply(option) && TRUE.equals(field.get(arguments))) {
                    return true;
                }
            }
        } catch (final IllegalAccessException ignored)
        { /* can't happen so lets just say false */ }

        return false;
    }

}
