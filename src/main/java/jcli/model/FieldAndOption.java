package jcli.model;

import jcli.annotations.CliOption;

import java.lang.reflect.Field;

/**
 * The FieldAndOption class.
 */
public final class FieldAndOption {
    /**
     * The field.
     */
    public final Field field;
    /**
     * The option.
     */
    public final CliOption option;

    /**
     * FieldAndOption.
     * @param field the field
     * @param option the option
     */
    public FieldAndOption(final Field field, final CliOption option) {
        this.field = field;
        this.option = option;
    }
}
