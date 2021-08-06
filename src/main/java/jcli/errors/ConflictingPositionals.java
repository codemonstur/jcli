package jcli.errors;

public final class ConflictingPositionals extends InvalidPositionalConfiguration {
    public ConflictingPositionals() {
        super("There is a List positional and other positionals at the same time");
    }
}
