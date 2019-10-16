package jcli.errors;

public final class TooManyPositionalArguments extends InvalidCommandLine {
    public TooManyPositionalArguments() {
        super("You have provided too many positional arguments");
    }
}
