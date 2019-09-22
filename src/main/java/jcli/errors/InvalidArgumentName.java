package jcli.errors;

public final class InvalidArgumentName extends InvalidArgumentConfiguration {
    public InvalidArgumentName(final String fieldName) {
        super("Arguments must set either a name or a long name. Field name is " + fieldName);
    }
}
