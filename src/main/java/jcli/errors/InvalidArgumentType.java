package jcli.errors;

import jcli.Argument;

public final class InvalidArgumentType extends InvalidArgumentConfiguration {
    public InvalidArgumentType(final Argument argument) {
        super("Argument " + argument.name() + " has an invalid type");
    }
}
