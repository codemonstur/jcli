package jcli.errors;

import java.lang.reflect.Field;

public final class InvalidModifierFinal extends InvalidOptionConfiguration {
    public InvalidModifierFinal(final Field field) {
        super("The field " + field.getName() + " is declared final. This is not supported. For details you can find a comment in the Parser code.");
    }
}
