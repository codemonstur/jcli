package jcli.errors;

import jcli.CliOption;

public final class MissingArgument extends InvalidCommandLine {
    public MissingArgument(final CliOption argument) {
        super("Missing argument " + argument.longName());
    }
}
