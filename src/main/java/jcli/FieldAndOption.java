package jcli;

import java.lang.reflect.Field;

public final class FieldAndOption {
    public final Field field;
    public final CliOption option;

    public FieldAndOption(final Field field, final CliOption option) {
        this.field = field;
        this.option = option;
    }
}
