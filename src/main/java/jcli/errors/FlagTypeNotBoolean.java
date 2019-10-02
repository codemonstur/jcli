package jcli.errors;

import java.lang.reflect.Field;

public final class FlagTypeNotBoolean extends InvalidOptionConfiguration {
    public FlagTypeNotBoolean(final Field field) {
        super("isFlag set on field that is not a boolean: " + field.getName());
    }
}
