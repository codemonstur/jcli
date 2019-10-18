package jcli.model;

import jcli.annotations.CliPositional;

import java.lang.reflect.Field;

public final class FieldAndPosition {
    public final Field field;
    public final CliPositional position;

    public FieldAndPosition(final Field field, final CliPositional position) {
        this.field = field;
        this.position = position;
    }
}
