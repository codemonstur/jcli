package jcli.errors;

import jcli.Argument;

public final class MissingArgument extends InvalidCommandLine {
    public MissingArgument(final Argument argument) {
        super("Missing argument " + argument.longName());
    }
}
