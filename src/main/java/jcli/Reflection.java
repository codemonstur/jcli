package jcli;

import java.lang.reflect.Field;
import java.util.*;

public enum Reflection {;

    public static List<Field> listFields(final Class<?> type) {
        return listFields(new ArrayList<>(), type);
    }
    public static List<Field> listFields(final List<Field> fields, final Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) listFields(fields, type.getSuperclass());
        return fields;
    }

    private static final Set<Class<?>> VALID_FIELDS = new HashSet<>(Arrays.asList(String.class, Double.class
            , double.class, Integer.class, int.class, Float.class, float.class, Byte.class, byte.class
            , Character.class, char.class, Short.class, short.class, Long.class, long.class, Boolean.class
            , boolean.class));
    public static boolean isValidFieldType(final Class<?> type) {
        return VALID_FIELDS.contains(type);
    }

    public static Object toFieldType(final Class<?> type, final String value) {
        if (type.isAssignableFrom(String.class)) return value;
        if (type.isAssignableFrom(double.class)) return Double.valueOf(value);
        if (type.isAssignableFrom(Double.class)) return Double.valueOf(value);
        if (type.isAssignableFrom(int.class)) return Integer.valueOf(value);
        if (type.isAssignableFrom(Integer.class)) return Integer.valueOf(value);
        if (type.isAssignableFrom(float.class)) return Float.valueOf(value);
        if (type.isAssignableFrom(Float.class)) return Float.valueOf(value);
        if (type.isAssignableFrom(byte.class)) return Byte.valueOf(value);
        if (type.isAssignableFrom(Byte.class)) return Byte.valueOf(value);
        if (type.isAssignableFrom(char.class)) return value.charAt(0);
        if (type.isAssignableFrom(Character.class)) return value.charAt(0);
        if (type.isAssignableFrom(short.class)) return Short.valueOf(value);
        if (type.isAssignableFrom(Short.class)) return Short.valueOf(value);
        if (type.isAssignableFrom(long.class)) return Long.valueOf(value);
        if (type.isAssignableFrom(Long.class)) return Long.valueOf(value);
        if (type.isAssignableFrom(boolean.class)) return Boolean.valueOf(value);
        if (type.isAssignableFrom(Boolean.class)) return Boolean.valueOf(value);
        return null;
    }

    public static boolean isBooleanType(final Field field) {
        return field.getType().equals(Boolean.class) || field.getType().equals(boolean.class);
    }
}
