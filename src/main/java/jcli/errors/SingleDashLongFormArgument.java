package jcli.errors;

public final class SingleDashLongFormArgument extends InvalidCommandLine {
    public SingleDashLongFormArgument(final String arg) {
        super("Long form arguments must start with two dashes, " + arg + " does not");
    }
}
