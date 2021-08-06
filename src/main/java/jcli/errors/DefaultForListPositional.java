package jcli.errors;

public final class DefaultForListPositional extends InvalidPositionalConfiguration {
    public DefaultForListPositional() {
        super("Found a list positional with a default value configured");
    }
}
