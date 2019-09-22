package jcli.errors;

public abstract class InvalidArgumentConfiguration extends Exception {
    protected InvalidArgumentConfiguration(String message) {
        super(message);
    }
}
