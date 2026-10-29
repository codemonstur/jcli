package jcli.errors;

import jcli.annotations.CliOption;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.lang.String.format;

/**
 * The InvalidCommandLine class.
 */
public final class InvalidCommandLine extends Exception {

    /** The key of the generic 'did you mean' hint that is appended when a suggestion is attached */
    private static final String SUGGESTION_KEY = "error.didYouMean";

    /** The {}. */
    private static final Object[] NO_PARAMS = {};

    /** The defaultMessage. */
    private final String defaultMessage;
    /** The key. */
    private final String key;
    /** The params. */
    private final Object[] params;
    /** The bundle. */
    private ResourceBundle bundle;
    /** The suggestion. */
    private String suggestion;

    /**
     * InvalidCommandLine.
     * @param key the key
     * @param params the params
     * @param defaultMessage the defaultMessage
     */
    private InvalidCommandLine(final String key, final Object[] params, final String defaultMessage) {
        super(defaultMessage);
        this.key = key;
        this.params = params;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Attaches a resource bundle used to translate this error. Returns the same instance.
      * @param bundle the bundle
      * @return the result
     */
    public InvalidCommandLine localize(final ResourceBundle bundle) {
        this.bundle = bundle;
        return this;
    }

    /**
     * Attaches a 'did you mean' suggestion to this error. Returns the same instance.
      * @param suggestion the suggestion
      * @return the result
     */
    public InvalidCommandLine suggest(final String suggestion) {
        this.suggestion = suggestion;
        return this;
    }

    @Override
    /**
     * getMessage.
     * @return the result
     */
    public String getMessage() {
        final String message = bundle == null || key == null || !bundle.containsKey(key)
            ? defaultMessage
            : formatFromBundle(key, params);
        return suggestion == null ? message : message + ". " + hint();
    }

    /**
     * formatFromBundle.
     * @param key the key
     * @param params the params
     * @return the result
     */
    private String formatFromBundle(final String key, final Object[] params) {
        try {
            return MessageFormat.format(bundle.getString(key), params);
        } catch (final MissingResourceException e) {
            return defaultMessage;
        }
    }

    /**
     * hint.
     * @return the result
     */
    private String hint() {
        return bundle != null && bundle.containsKey(SUGGESTION_KEY)
            ? formatFromBundle(SUGGESTION_KEY, new Object[]{suggestion})
            : format("Did you mean '%s'?", suggestion);
    }

    /**
     * newAttachedFormFlagArgument.
     * @return the result
     */
    public static InvalidCommandLine newAttachedFormFlagArgument() {
        return new InvalidCommandLine("error.flagAttached", NO_PARAMS, "Flag arguments do not use attached long form");
    }
    /**
     * newCommandLineArgumentTooShort.
     * @return the result
     */
    public static InvalidCommandLine newCommandLineArgumentTooShort() {
        return new InvalidCommandLine("error.argumentTooShort", NO_PARAMS, "A command line option must have a name, '-' or '--' is not valid");
    }
    /**
     * newInvalidArgumentValue.
     * @param value the value
     * @return the result
     */
    public static InvalidCommandLine newInvalidArgumentValue(final String value) {
        return new InvalidCommandLine("error.invalidValue", new Object[]{value}
                , format("The command line value '%s' has the wrong format", value));
    }
    /**
     * newInvalidCommandLineArgument.
     * @param arg the arg
     * @return the result
     */
    public static InvalidCommandLine newInvalidCommandLineArgument(final String arg) {
        return new InvalidCommandLine("error.notACommandLineArgument", new Object[]{arg}
                , "Command line arguments must start with '-', " + arg + " does not");
    }
    /**
     * newMissingArgument.
     * @param argument the argument
     * @return the result
     */
    public static InvalidCommandLine newMissingArgument(final CliOption argument) {
        return new InvalidCommandLine("error.missingArgument", new Object[]{toName(argument)}
                , format("Missing argument '%s'", toName(argument)));
    }
    /**
     * newMissingArgument.
     * @param positional the positional
     * @return the result
     */
    public static InvalidCommandLine newMissingArgument(final Field positional) {
        return new InvalidCommandLine("error.missingPositional", new Object[]{positional.getName()}
                , "Missing positional argument " + positional.getName());
    }
    /**
     * newDuplicateCommandLineArgument.
     * @param arg the arg
     * @return the result
     */
    public static InvalidCommandLine newDuplicateCommandLineArgument(final String arg) {
        return new InvalidCommandLine("error.duplicateArgument", new Object[]{arg}
                , "'" + arg + "' was provided more than once");
    }
    /**
     * newMissingCommandLineValue.
     * @param arg the arg
     * @return the result
     */
    public static InvalidCommandLine newMissingCommandLineValue(final String arg) {
        return new InvalidCommandLine("error.missingValue", new Object[]{arg}
                , "Argument '" + arg + "' is missing a value");
    }
    /**
     * newSingleDashAttachedFormArgument.
     * @param arg the arg
     * @return the result
     */
    public static InvalidCommandLine newSingleDashAttachedFormArgument(final String arg) {
        return new InvalidCommandLine("error.singleDash", new Object[]{arg}
                , "Long form arguments must start with two dashes, '" + arg + "' does not");
    }
    /**
     * newTooManyPositionalArguments.
     * @param totalNumberOfPositionalArguments the totalNumberOfPositionalArguments
     * @param positionalArgumentIndex the positionalArgumentIndex
     * @return the result
     */
    public static InvalidCommandLine newTooManyPositionalArguments(final int totalNumberOfPositionalArguments, final int positionalArgumentIndex) {
        return new InvalidCommandLine("error.tooManyPositionals", new Object[]{totalNumberOfPositionalArguments, positionalArgumentIndex}
                , "You have provided too many positional arguments. There are " + totalNumberOfPositionalArguments + " but you have provided " + positionalArgumentIndex);
    }
    /**
     * newUnknownCommandLineArgument.
     * @param arg the arg
     * @return the result
     */
    public static InvalidCommandLine newUnknownCommandLineArgument(final String arg) {
        return new InvalidCommandLine("error.unknownArgument", new Object[]{arg}
                , "'" + arg + "' is not a valid command line argument");
    }

    /**
     * newInvalidValue.
     * @param name the name
     * @param value the value
     * @param expected the expected
     * @return the result
     */
    public static InvalidCommandLine newInvalidValue(final String name, final String value, final String expected) {
        return new InvalidCommandLine("error.invalidValueFor", new Object[]{value, name, expected}
                , format("The value '%s' for '%s' is not a valid %s", value, name, expected));
    }
    /**
     * newValueTooSmall.
     * @param name the name
     * @param value the value
     * @param min the min
     * @return the result
     */
    public static InvalidCommandLine newValueTooSmall(final String name, final String value, final String min) {
        return new InvalidCommandLine("error.tooSmall", new Object[]{value, name, min}
                , format("The value '%s' for '%s' must be at least %s", value, name, min));
    }
    /**
     * newValueTooLarge.
     * @param name the name
     * @param value the value
     * @param max the max
     * @return the result
     */
    public static InvalidCommandLine newValueTooLarge(final String name, final String value, final String max) {
        return new InvalidCommandLine("error.tooLarge", new Object[]{value, name, max}
                , format("The value '%s' for '%s' must be at most %s", value, name, max));
    }
    /**
     * newDisallowedValue.
     * @param name the name
     * @param value the value
     * @param allowed the allowed
     * @return the result
     */
    public static InvalidCommandLine newDisallowedValue(final String name, final String value, final String allowed) {
        return new InvalidCommandLine("error.disallowedValue", new Object[]{value, name, allowed}
                , format("The value '%s' for '%s' is not allowed, it must be one of: %s", value, name, allowed));
    }
    /**
     * newInvalidRegexValue.
     * @param name the name
     * @param value the value
     * @param regex the regex
     * @return the result
     */
    public static InvalidCommandLine newInvalidRegexValue(final String name, final String value, final String regex) {
        return new InvalidCommandLine("error.invalidRegexValue", new Object[]{value, name, regex}
                , format("The value '%s' for '%s' does not match the pattern %s", value, name, regex));
    }
    /**
     * newInvalidBooleanValue.
     * @param name the name
     * @param value the value
     * @return the result
     */
    public static InvalidCommandLine newInvalidBooleanValue(final String name, final String value) {
        return new InvalidCommandLine("error.invalidBoolean", new Object[]{value, name}
                , format("The value '%s' for '%s' must be true or false", value, name));
    }
    /**
     * newMissingArityValues.
     * @param name the name
     * @param expected the expected
     * @param actual the actual
     * @return the result
     */
    public static InvalidCommandLine newMissingArityValues(final String name, final int expected, final int actual) {
        return new InvalidCommandLine("error.missingArityValues", new Object[]{name, expected, actual}
                , format("The option '%s' expects %s values but only %s were given", name, expected, actual));
    }
    /**
     * newMissingRequiredArgument.
     * @param name the name
     * @param required the required
     * @return the result
     */
    public static InvalidCommandLine newMissingRequiredArgument(final String name, final String required) {
        return new InvalidCommandLine("error.requires", new Object[]{name, required}
                , format("The option '%s' requires '%s'", name, required));
    }
    /**
     * newExcludedArgument.
     * @param name the name
     * @param excluded the excluded
     * @return the result
     */
    public static InvalidCommandLine newExcludedArgument(final String name, final String excluded) {
        return new InvalidCommandLine("error.excludes", new Object[]{name, excluded}
                , format("The option '%s' cannot be used together with '%s'", name, excluded));
    }
    /**
     * newUnknownSubcommand.
     * @param name the name
     * @return the result
     */
    public static InvalidCommandLine newUnknownSubcommand(final String name) {
        return new InvalidCommandLine("error.unknownSubcommand", new Object[]{name}
                , "'" + name + "' is not a valid command");
    }
    /**
     * newPromptFailed.
     * @param name the name
     * @return the result
     */
    public static InvalidCommandLine newPromptFailed(final String name) {
        return new InvalidCommandLine("error.promptFailed", new Object[]{name}
                , format("Could not read a value for '%s'", name));
    }

    /**
     * toName.
     * @param argument the argument
     * @return the result
     */
    private static String toName(final CliOption argument) {
        return argument.longName().isEmpty() ? "-" + argument.name() : "--" + argument.longName();
    }

}
