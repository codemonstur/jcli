package jcli.errors;

import java.lang.reflect.Field;

public final class InvalidModifierStatic extends InvalidOptionConfiguration {
    public InvalidModifierStatic(final Field field) {
        super("The field " + field.getName() + " is declared static. Annotated fields cannot be static.");
    }
}