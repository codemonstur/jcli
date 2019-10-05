package jcli.errors;

public final class AttachedFormFlagArgument extends InvalidCommandLine {
    public AttachedFormFlagArgument() {
        super("Flag arguments do not use long form");
    }
}
