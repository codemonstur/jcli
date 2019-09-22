package jcli.errors;

public abstract class InvalidOptionConfiguration extends RuntimeException {
    protected InvalidOptionConfiguration(final String message) {
        super(message);
    }
}
