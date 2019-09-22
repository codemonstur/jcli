package jcli.errors;

public final class CommandLineOptionTooShort extends InvalidCommandLine {
    public CommandLineOptionTooShort() {
        super("A command line option must have a name, '-' or '--' is not valid");
    }
}
