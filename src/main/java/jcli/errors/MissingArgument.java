package jcli.errors;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

public final class MissingArgument extends InvalidCommandLine {
    public MissingArgument(final CliOption argument) {
        super("Missing argument " + argument.longName());
    }
    public MissingArgument(final CliPositional positional) {
        super("Missing positional argument " + positional.index());
    }
}
