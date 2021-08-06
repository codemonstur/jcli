package jcli.errors;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

import java.lang.reflect.Field;

public final class InvalidOptionType extends InvalidOptionConfiguration {
    public InvalidOptionType(final CliOption option) {
        super("Option " + (option.name() == ' ' ? option.longName() : option.name()) + " has an invalid type");
    }
    public InvalidOptionType(final Field positional) {
        super("Positional " + positional.getName() + " has an invalid type");
    }
}
