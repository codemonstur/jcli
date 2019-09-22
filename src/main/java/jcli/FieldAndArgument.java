package jcli;

import java.lang.reflect.Field;

public final class FieldAndArgument {
    public final Field field;
    public final Argument argument;

    public FieldAndArgument(final Field field, final Argument argument) {
        this.field = field;
        this.argument = argument;
    }
}
