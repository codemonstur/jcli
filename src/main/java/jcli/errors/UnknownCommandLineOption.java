package jcli.errors;

public final class UnknownCommandLineOption extends InvalidCommandLine {
    public UnknownCommandLineOption(final String arg) {
        super(arg + " is not a valid command line option.");
    }
}
