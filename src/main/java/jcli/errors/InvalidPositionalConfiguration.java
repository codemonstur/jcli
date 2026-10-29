package jcli.errors;

/**
 * The InvalidPositionalConfiguration class.
 */
public final class InvalidPositionalConfiguration extends RuntimeException {
    /**
     * InvalidPositionalConfiguration.
     * @param message the message
     */
    private InvalidPositionalConfiguration(final String message) {
        super(message);
    }

    /**
     * newConflictingPositionals.
     * @return the result
     */
    public static InvalidPositionalConfiguration newConflictingPositionals() {
        return new InvalidPositionalConfiguration("There is a List positional and other positionals at the same time");
    }
    /**
     * newDefaultForListPositional.
     * @return the result
     */
    public static InvalidPositionalConfiguration newDefaultForListPositional() {
        return new InvalidPositionalConfiguration("Found a list positional with a default value configured");
    }
    /**
     * newMandatoryPositionalAfterOptional.
     * @return the result
     */
    public static InvalidPositionalConfiguration newMandatoryPositionalAfterOptional() {
        return new InvalidPositionalConfiguration("Found a mandatory positional after an optional positional");
    }

}
