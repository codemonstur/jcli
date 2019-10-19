package jcli.errors;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

public final class InvalidOptionType extends InvalidOptionConfiguration {
    public InvalidOptionType(final CliOption option) {
        super("Option " + (option.name() == ' ' ? option.longName() : option.name()) + " has an invalid type");
    }
    public InvalidOptionType(final CliPositional positional) {
        super("Positional " + positional.index() + " has an invalid type");
    }
}
