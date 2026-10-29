package jcli.model;

import jcli.annotations.CliPositional;

import java.lang.reflect.Field;

/**
 * The FieldAndPosition class.
 */
public final class FieldAndPosition {
    /**
     * The field.
     */
    public final Field field;
    /**
     * The position.
     */
    public final CliPositional position;

    /**
     * FieldAndPosition.
     * @param field the field
     * @param position the position
     */
    public FieldAndPosition(final Field field, final CliPositional position) {
        this.field = field;
        this.position = position;
    }
}
