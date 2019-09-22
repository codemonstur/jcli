package jcli.errors;

public abstract class InvalidCommandLine extends Exception {
    protected InvalidCommandLine(String message) {
        super(message);
    }
}
