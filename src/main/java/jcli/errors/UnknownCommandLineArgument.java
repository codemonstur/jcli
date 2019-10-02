package jcli.errors;

public final class UnknownCommandLineArgument extends InvalidCommandLine {
    public UnknownCommandLineArgument(final String arg) {
        super(arg + " is not a valid command line argument.");
    }
}
