package jcli.errors;

import jcli.annotations.CliOption;

import java.lang.reflect.Field;

import static java.lang.String.format;

public final class InvalidCommandLine extends Exception {
    private InvalidCommandLine(final String message) {
        super(message);
    }

    public static InvalidCommandLine newAttachedFormFlagArgument() {
        return new InvalidCommandLine("Flag arguments do not use attached long form");
    }
    public static InvalidCommandLine newCommandLineArgumentTooShort() {
        return new InvalidCommandLine("A command line option must have a name, '-' or '--' is not valid");
    }
    public static InvalidCommandLine newInvalidArgumentValue(final String value) {
        return new InvalidCommandLine(format("The command line value '%s' has the wrong format", value));
    }
    public static InvalidCommandLine newInvalidCommandLineArgument(final String arg) {
        return new InvalidCommandLine("Command line arguments must start with '-', " + arg + " does not");
    }
    public static InvalidCommandLine newMissingArgument(final CliOption argument) {
        return new InvalidCommandLine("Missing argument '" + argument.longName() + "'");
    }
    public static InvalidCommandLine newMissingArgument(final Field positional) {
        return new InvalidCommandLine("Missing positional argument " + positional.getName());
    }
    public static InvalidCommandLine newMissingCommandLineValue(final String arg) {
        return new InvalidCommandLine("Argument '" + arg + "' is missing a value");
    }
    public static InvalidCommandLine newSingleDashAttachedFormArgument(final String arg) {
        return new InvalidCommandLine("Long form arguments must start with two dashes, '" + arg + "' does not");
    }
    public static InvalidCommandLine newTooManyPositionalArguments(final int totalNumberOfPositionalArguments, final int positionalArgumentIndex) {
        return new InvalidCommandLine("You have provided too many positional arguments. There are " + totalNumberOfPositionalArguments + " but you have provided " + positionalArgumentIndex);
    }
    public static InvalidCommandLine newUnknownCommandLineArgument(final String arg) {
        return new InvalidCommandLine("'" + arg + "' is not a valid command line argument");
    }

}
