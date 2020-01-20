package jcli;

import jcli.annotations.CliUnderscoreIsDash;
import jcli.errors.InvalidArgumentValue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

public enum Reflection {;

    public interface StringToType<T> {
        T toType(String value);
    }
    public interface ToFieldType {
        Object toFieldType(Class<?> type, String value) throws InvalidArgumentValue;
    }

    /*
     * This code used to remove the final modifier from the field using this code:
     *
     *    Field modifiersField = Field.class.getDeclaredField("modifiers");
     *    modifiersField.setAccessible(true);
     *    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
     *
     * However, since Java 12 this hack no longer works. Instead you will get an exception:
     *
     *    java.lang.NoSuchFieldException: modifiers
     *
     * There is a workaround available for Java 12 using a VarHandle documented here:
     * https://stackoverflow.com/questions/56039341/get-declared-fields-of-java-lang-reflect-fields-in-jdk12/56042394#56042394
     * VarHandles were introduced in Java 9.
     *
     * We therefore have these options:
     *
     *  1. Use the original hack, making the library unusable in Java 12 and forwards
     *  2. Use the new hack, making the library unusable in Java 8 and lower
     *  3. Don't remove the final modifier, forcing users to provide a class with mutable fields
     *
     * I have chosen 3 for now, if a different solution presents itself I will revisit the decision.
     */
    public static void makeAccessible(final Field field) {
        field.setAccessible(true);
    }

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
            , boolean.class, File.class, BigDecimal.class, BigInteger.class, URI.class, Path.class, Instant.class
            , LocalTime.class, LocalDate.class, LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class
            , OptionalDouble.class, OptionalInt.class, OptionalLong.class, Pattern.class, Charset.class, List.class
            ));
    public static boolean isValidFieldType(final Class<?> type) {
        return VALID_FIELDS.contains(type) || type.isEnum();
    }


    public static Object toFieldType(final Class<?> type, final String value) throws InvalidArgumentValue {
        try {
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
            if (type.isAssignableFrom(File.class)) return new File(value);
            if (type.isAssignableFrom(BigDecimal.class)) return new BigDecimal(value);
            if (type.isAssignableFrom(BigInteger.class)) return new BigInteger(value);
            if (type.isAssignableFrom(URI.class)) return new URI(value);
            if (type.isAssignableFrom(Path.class)) return Paths.get(value);
            if (type.isAssignableFrom(Instant.class)) return Instant.parse(value);
            if (type.isAssignableFrom(LocalTime.class)) return LocalTime.parse(value);
            if (type.isAssignableFrom(LocalDate.class)) return LocalDate.parse(value);
            if (type.isAssignableFrom(LocalDateTime.class)) return LocalDateTime.parse(value);
            if (type.isAssignableFrom(OffsetDateTime.class)) return OffsetDateTime.parse(value);
            if (type.isAssignableFrom(ZonedDateTime.class)) return ZonedDateTime.parse(value);
            if (type.isAssignableFrom(OptionalDouble.class)) {
                return value == null || value.isEmpty()
                     ? OptionalDouble.empty()
                     : OptionalDouble.of(Double.parseDouble(value));
            }
            if (type.isAssignableFrom(OptionalInt.class)) {
                return value == null || value.isEmpty()
                     ? OptionalInt.empty()
                     : OptionalInt.of(Integer.parseInt(value));
            }
            if (type.isAssignableFrom(OptionalLong.class)) {
                return value == null || value.isEmpty()
                     ? OptionalLong.empty()
                     : OptionalLong.of(Long.parseLong(value));
            }
            if (type.isAssignableFrom(Pattern.class)) return Pattern.compile(value);
            if (type.isAssignableFrom(Charset.class)) return Charset.forName(value);
            if (type.isEnum()) {
                String enumName = value;
                if (type.isAnnotationPresent(CliUnderscoreIsDash.class))
                    enumName = enumName.replaceAll("-", "_");
                return Enum.valueOf((Class<Enum>)type, enumName);
            }
        } catch (Exception e) {
            throw new InvalidArgumentValue(value);
        }
        return null;
    }

    public static boolean isBooleanType(final Field field) {
        return field.getType().equals(Boolean.class) || field.getType().equals(boolean.class);
    }
    public static boolean isListType(final Field field) {
        return field.getType().equals(List.class);
    }

    public static Class<?> toParameterType(final Field field) {
        return (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
    }

//    public static boolean hasMissingDefault(final CliOption option, final Field field) {
//        if (field.getType().equals(List.class)) return false;
//        return !option.isMandatory() && !isBooleanType(field) && !option.isHelp() && option.defaultValue().equals(FAKE_NULL);
//    }

}
