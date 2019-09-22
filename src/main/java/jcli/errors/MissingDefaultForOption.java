package jcli.errors;

import jcli.CliOption;

public final class MissingDefaultForOption extends InvalidOptionConfiguration {
    public MissingDefaultForOption(final CliOption argument) {
        super("The argument " + argument.name() + " is optional but has no default.");
    }
}
