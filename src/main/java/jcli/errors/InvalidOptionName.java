package jcli.errors;

public final class InvalidOptionName extends InvalidOptionConfiguration {
    public InvalidOptionName(final String fieldName) {
        super("Arguments must set either a name or a long name. Field name is " + fieldName);
    }
}
