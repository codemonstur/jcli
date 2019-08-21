package jcli;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Reflection {;

    public static List<Field> listFields(final Class<?> type) {
        return listFields(new ArrayList<>(), type);
    }
    public static List<Field> listFields(final List<Field> fields, final Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) listFields(fields, type.getSuperclass());
        return fields;
    }

}
