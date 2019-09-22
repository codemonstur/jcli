package jcli.errors;

import jcli.CliOption;

public final class InvalidOptionType extends InvalidOptionConfiguration {
    public InvalidOptionType(final CliOption argument) {
        super("Argument " + argument.name() + " has an invalid type");
    }
}
