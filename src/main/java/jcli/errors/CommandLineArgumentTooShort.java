package jcli.errors;

public final class CommandLineArgumentTooShort extends InvalidCommandLine {
    public CommandLineArgumentTooShort() {
        super("A command line option must have a name, '-' or '--' is not valid");
    }
}
