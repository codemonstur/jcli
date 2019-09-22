package jcli.errors;

public final class LongFormFlagArgument extends InvalidCommandLine {
    public LongFormFlagArgument() {
        super("Flag arguments do not use long form");
    }
}
