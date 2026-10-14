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

public class CliParserBuilder<T> {

    public interface CliErrorConsumer {
        void cliArgumentsInvalid(InvalidCommandLine e, String[] args);
    }
    public interface CliHelpConsumer<A> {
        void cliHelpRequested(A arguments, String[] args);
    }
    public interface CliVersionConsumer {
        void printVersion(String vendor, String name, String version);
    }

    public static <U> CliParserBuilder<U> newCliParser(final Supplier<U> supplier) {
        return new CliParserBuilder<U>().object(supplier);
    }
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

    public CliParserBuilder<T> name(final String name) {
        this.name = name;
        return this;
    }
    public CliParserBuilder<T> object(final Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }
    public CliParserBuilder<T> onErrorPrintHelpAndExit() {
        onErrorPrintHelpAndExit = true;
        return this;
    }
    public CliParserBuilder<T> onErrorCall(final CliErrorConsumer consumer) {
        onErrorCall = consumer;
        return this;
    }
    public CliParserBuilder<T> errorExitCode(final int code) {
        this.errorExitCode = code;
        return this;
    }
    public CliParserBuilder<T> onHelpPrintHelpAndExit() {
        onHelpPrintHelpAndExit = true;
        return this;
    }
    public CliParserBuilder<T> onHelpCall(final CliHelpConsumer<T> consumer) {
        onHelpCall = consumer;
        return this;
    }
    public CliParserBuilder<T> helpExitCode(final int code) {
        this.helpExitCode = code;
        return this;
    }
    public CliParserBuilder<T> helpConsumer(final Consumer<String> helpConsumer) {
        this.helpConsumer = helpConsumer;
        return this;
    }

    /** Where the '[ERROR] message' line of onErrorPrintHelpAndExit goes. Defaults to System.err. */
    public CliParserBuilder<T> errorConsumer(final Consumer<String> errorConsumer) {
        this.errorConsumer = errorConsumer;
        return this;
    }


    public CliParserBuilder<T> onVersionPrintVersionAndExit() {
        onVersionPrintVersionAndExit = true;
        return this;
    }
    public CliParserBuilder<T> onVersionCall(final CliVersionConsumer onVersionCall) {
        this.onVersionCall = onVersionCall;
        return this;
    }
    public CliParserBuilder<T> versionExitCode(final int code) {
        this.versionExitCode = code;
        return this;
    }
    public CliParserBuilder<T> versionConsumer(final CliVersionConsumer versionConsumer) {
        this.versionConsumer = versionConsumer;
        return this;
    }

    /**
     * Replaces System.exit, mostly useful in tests and when embedding the parser.
     * When the handler returns, the help and version paths continue normally and the error path rethrows.
     */
    public CliParserBuilder<T> exitHandler(final IntConsumer exitHandler) {
        this.exitHandler = exitHandler;
        return this;
    }

    public CliParserBuilder<T> indent(final String indent) {
        this.helpIndent = indent;
        return this;
    }

    /** Wraps the descriptions in the help output. Zero, the default, means no wrapping. */
    public CliParserBuilder<T> helpWidth(final int width) {
        this.helpWidth = width;
        return this;
    }

    /** Translates the built in help and error text. Keys that are missing fall back to English. */
    public CliParserBuilder<T> resourceBundle(final ResourceBundle bundle) {
        this.messages = new Messages(bundle);
        return this;
    }

    /** Supplies the value of options that are annotated with isInteractive. */
    public CliParserBuilder<T> prompter(final CliPrompter prompter) {
        this.prompter = prompter;
        return this;
    }

    /** Missing mandatory options and positionals are no longer an error. */
    public CliParserBuilder<T> ignoreMissingMandatory() {
        this.ignoreMissingMandatory = true;
        return this;
    }

    /** Arguments that look like an option but are not one are treated as positional arguments. */
    public CliParserBuilder<T> allowPrefixedPositionals() {
        this.allowPrefixedPositionals = true;
        return this;
    }

    /**
     * Values for options that are not on the command line, looked up by long name, short name or field name.
     * They beat the defaultValue of the annotation.
     */
    public CliParserBuilder<T> defaultsFrom(final Map<?, ?> map) {
        this.defaults = ParseContext.toDefaultsMap(map);
        return this;
    }
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

    /** Called when a deprecated option is used. Defaults to printing a warning on stderr. */
    public CliParserBuilder<T> deprecationConsumer(final Consumer<String> consumer) {
        this.deprecationConsumer = consumer;
        return this;
    }

    public <C> CliParserBuilder<T> addClassConverter(final Class<C> clazz, final StringToType<C> converter) {
        this.classConverters.put(clazz, converter);
        return this;
    }

    /**
     * A converter for one specific field, looked up by field name.
     * It beats the built in conversion and the class converters, and lets a field hold a type
     * that nothing else can convert.
     */
    public <D> CliParserBuilder<T> fieldConverter(final String fieldName, final StringToType<D> converter) {
        this.fieldConverters.put(fieldName, converter);
        return this;
    }

    /** Registers a subcommand: the first argument that is not an option selects it. */
    public CliParserBuilder<T> subcommand(final String name, final CliParserBuilder<?> parser) {
        return subcommand(name, "", parser);
    }
    public CliParserBuilder<T> subcommand(final String name, final String description, final CliParserBuilder<?> parser) {
        if (isNullOrEmpty(name) || name.startsWith("-"))
            throw newInvalidSubcommandName(name);
        if (parser == null)
            throw newMissingSubcommandParser(name);
        if (subcommands.containsKey(name))
            throw newDuplicateSubcommand(name);
        subcommands.put(name, new Subcommand(description == null ? "" : description, parser));
        return this;
    }

    public Map<String, String> getSubcommandDescriptions() {
        final Map<String, String> descriptions = new LinkedHashMap<>();
        for (final Map.Entry<String, Subcommand> entry : subcommands.entrySet())
            descriptions.put(entry.getKey(), entry.getValue().description);
        return descriptions;
    }

    /**
     * The help text with all the settings of this builder applied:
     * name, indent, width, resource bundle and subcommand descriptions.
     */
    public String getHelpText() {
        return getHelp(name, helpIndent, getArgumentsClass(), classConverters.keySet()
                , fieldConverters.keySet(), messages, helpWidth, getSubcommandDescriptions());
    }

    String getName() {
        return name;
    }

    Class<?> getArgumentsClass() {
        return supplier.get().getClass();
    }

    Map<String, Subcommand> getSubcommands() {
        return subcommands;
    }

    // Not pretty but so far the only solution to the checked Exception problem
    public T parseSuppressErrors(final String[] args) {
        try {
            return parse(args);
        } catch (final Exception e) {
            return null;
        }
    }

    public T parse(final String[] args) throws InvalidCommandLine {
        if (!subcommands.isEmpty())
            throw newInvalidSubcommandConfiguration();
        return parseWithSettings(args, supplier.get());
    }

    /**
     * Parses a command line that can contain subcommands.
     * The result holds the arguments of the root command and of every subcommand that was selected.
     */
    public CommandResult<T> parseWithSubcommands(final String[] args) throws InvalidCommandLine {
        @SuppressWarnings("unchecked")
        final CommandResult<T> result = (CommandResult<T>) parseInternal(args);
        return result;
    }

    private CommandResult<?> parseInternal(final String[] args) throws InvalidCommandLine {
        final Object object = supplier.get();
        final List<Object> chain = new ArrayList<>();
        final List<String> path = new ArrayList<>();

        int index = -1;
        if (!subcommands.isEmpty()) {
            if (!CliParser.toFieldAndPositionList(object.getClass(), classConverters.keySet()
                    , fieldConverters.keySet()).isEmpty())
                throw newSubcommandWithPositionals(isNullOrEmpty(name) ? object.getClass().getSimpleName() : name);
            final Map<String, FieldAndOption> options = CliParser.toFieldAndOptionMap(object.getClass()
                    , classConverters.keySet(), fieldConverters.keySet());
            index = CliParser.findSubcommandIndex(args, options);
        }

        chain.add(parseWithSettings(index == -1 ? args : Arrays.copyOfRange(args, 0, index), object));

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

    private <A> A parseWithSettings(final String[] args, final A object) throws InvalidCommandLine {
        try {
            final A arguments = parseCommandLineArguments(args, object, parseContext());
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

    private ParseContext parseContext() {
        return new ParseContext(classConverters.keySet(), messages, prompter, newFieldToType()
                , ignoreMissingMandatory, allowPrefixedPositionals, defaults, fieldConverters, deprecationConsumer);
    }

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

    private <A> boolean isVersionSelected(final A arguments) {
        return hasFlagSet(arguments, CliOption::isVersion);
    }

    private <A> boolean isHelpSelected(final A arguments) {
        return hasFlagSet(arguments, CliOption::isHelp);
    }

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
