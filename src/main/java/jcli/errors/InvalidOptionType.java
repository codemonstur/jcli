package jcli.errors;

import jcli.CliOption;

public final class InvalidOptionType extends InvalidOptionConfiguration {
    public InvalidOptionType(final CliOption option) {
        super("Option " + option.name() + " has an invalid type");
    }
}
