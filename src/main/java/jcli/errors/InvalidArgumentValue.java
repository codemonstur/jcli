package jcli.errors;

public final class InvalidArgumentValue extends InvalidCommandLine {
    public InvalidArgumentValue(final String value) {
        super(String.format("The command line value '%s' has the wrong format", value));
    }
}
