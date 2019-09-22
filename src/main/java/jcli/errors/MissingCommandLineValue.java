package jcli.errors;

public final class MissingCommandLineValue extends InvalidCommandLine {
    public MissingCommandLineValue(final String arg) {
        super("Option " + arg + " is missing a value");
    }
}
