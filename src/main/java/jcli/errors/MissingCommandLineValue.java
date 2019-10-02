package jcli.errors;

public final class MissingCommandLineValue extends InvalidCommandLine {
    public MissingCommandLineValue(final String arg) {
        super("Argument " + arg + " is missing a value");
    }
}
