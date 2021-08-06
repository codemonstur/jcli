package jcli.errors;

public final class MandatoryPositionalAfterOptional extends InvalidPositionalConfiguration {
    public MandatoryPositionalAfterOptional() {
        super("Found a mandatory positional after an optional positional");
    }
}
