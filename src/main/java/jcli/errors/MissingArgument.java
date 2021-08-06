package jcli.errors;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

import java.lang.reflect.Field;

public final class MissingArgument extends InvalidCommandLine {
    public MissingArgument(final CliOption argument) {
        super("Missing argument " + argument.longName());
    }
    public MissingArgument(final Field positional) {
        super("Missing positional argument " + positional.getName());
    }
}
