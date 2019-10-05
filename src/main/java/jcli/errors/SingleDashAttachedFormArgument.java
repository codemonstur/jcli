package jcli.errors;

public final class SingleDashAttachedFormArgument extends InvalidCommandLine {
    public SingleDashAttachedFormArgument(final String arg) {
        super("Long form arguments must start with two dashes, " + arg + " does not");
    }
}
