package jcli.errors;

import java.lang.reflect.Field;

public final class HelpTypeNotBoolean extends InvalidOptionConfiguration {
    public HelpTypeNotBoolean(final Field field) {
        super("isHelp set on field that is not a boolean: " + field.getName());
    }
}
