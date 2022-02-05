package jcli.errors;

public final class InvalidPositionalConfiguration extends RuntimeException {
    private InvalidPositionalConfiguration(final String message) {
        super(message);
    }

    public static InvalidPositionalConfiguration newConflictingPositionals() {
        return new InvalidPositionalConfiguration("There is a List positional and other positionals at the same time");
    }
    public static InvalidPositionalConfiguration newDefaultForListPositional() {
        return new InvalidPositionalConfiguration("Found a list positional with a default value configured");
    }
    public static InvalidPositionalConfiguration newMandatoryPositionalAfterOptional() {
        return new InvalidPositionalConfiguration("Found a mandatory positional after an optional positional");
    }

}