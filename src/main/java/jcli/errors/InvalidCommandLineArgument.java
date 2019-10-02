package jcli.errors;

public final class InvalidCommandLineArgument extends InvalidCommandLine {
    public InvalidCommandLineArgument(final String arg) {
        super("Command line arguments must start with '-', " + arg + " does not");
    }
}
