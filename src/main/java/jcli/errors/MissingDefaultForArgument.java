package jcli.errors;

import jcli.Argument;

public final class MissingDefaultForArgument extends InvalidArgumentConfiguration {
    public MissingDefaultForArgument(final Argument argument) {
        super("The argument " + argument.name() + " is optional but has no default.");
    }
}
