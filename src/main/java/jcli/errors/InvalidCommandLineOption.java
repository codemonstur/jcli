package jcli.errors;

public final class InvalidCommandLineOption extends InvalidCommandLine {
    public InvalidCommandLineOption(final String arg) {
        super("Command line options must start with '-', " + arg + " does not");
    }
}
