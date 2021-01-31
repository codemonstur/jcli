package jcli.errors;

public abstract class InvalidCommandLine extends Exception {
    protected InvalidCommandLine(final String message) {
        super(message);
    }
}
