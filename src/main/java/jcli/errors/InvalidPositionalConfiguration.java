package jcli.errors;

public abstract class InvalidPositionalConfiguration extends RuntimeException {
    protected InvalidPositionalConfiguration(final String message) {
        super(message);
    }
}