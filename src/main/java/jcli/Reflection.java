package jcli;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.annotations.CliUnderscoreIsDash;
import jcli.errors.InvalidCommandLine;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

import static jcli.errors.InvalidCommandLine.newInvalidArgumentValue;

public enum Reflection {;

    public interface StringToType<T> {
        T toType(String value);
    }
    public interface ToFieldType {
        Object toFieldType(Field field, Class<?> type, String value) throws InvalidCommandLine;
    }

    /**
     * Fields must be made accessible before the parser can read or write them.
     *
     * This also grants write access to final fields: since Java 9 {@code Field.set} can
     * write a non-static final field once {@code setAccessible(true)} has succeeded, and
     * this library requires Java 17, so final fields on an arguments class are supported.
     * An earlier version of this library rejected final fields instead, because before
     * Java 9 the only way to write them was to strip the final modifier from the Field,
     * a hack that stopped working in Java 12.
     *
     * Two caveats apply:
     *
     *  - Final fields must be assigned in the constructor. A field that is initialized
     *    with a compile-time constant (for example {@code final int count = 0;}) is
     *    inlined at every read site by the compiler, and reads will keep returning the
     *    constant no matter what the parser writes.
     *  - Records cannot be used as an arguments class: all their fields are final and the
     *    JDK refuses reflective writes to record components outright.
     *
     * From Java 27 mutating a final field prints a warning, and a future release will
     * require the {@code --enable-final-field-mutation=ALL-UNNAMED} flag for it.
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
            , Optional.class
            ));
    public static boolean isValidFieldType(final Class<?> type) {
        return VALID_FIELDS.contains(type) || type.isEnum();
    }
    public static boolean isValidFieldType(final Class<?> type, final Collection<Class<?>> convertedTypes) {
        return convertedTypes.contains(type) || isValidFieldType(type);
    }
    public static boolean isValidListType(final Field field, final Collection<Class<?>> convertedTypes) {
        if (!isListType(field)) return true;
        // A raw list has no type information at all, it behaves like a List<String>
        if (!(field.getGenericType() instanceof ParameterizedType)) return true;
        final Type[] typeArguments = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
        if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class)) return false;
        final Class<?> elementType = (Class<?>)typeArguments[0];
        return !List.class.equals(elementType) && isValidFieldType(elementType, convertedTypes);
    }

    public static Object toFieldType(final Field field, final Class<?> type, final String value) throws InvalidCommandLine {
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
            if (type.isAssignableFrom(Optional.class)) {
                if (field == null) throw newInvalidArgumentValue("Optional type as generic type");
                if (value == null) return Optional.empty();
                return Optional.of(toFieldType(field, getTypeOfGenericField(field, Object.class), value));
            }
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
            throw newInvalidArgumentValue(value);
        }
        throw new IllegalArgumentException("Coding error, none of the types that we support matched the field type.");
    }

    public static boolean isBooleanType(final Field field) {
        return field.getType().equals(Boolean.class) || field.getType().equals(boolean.class);
    }
    public static boolean isListType(final Field field) {
        return field.getType().equals(List.class);
    }

    public static boolean isNumericType(final Class<?> type) {
        return byte.class.equals(type) || Byte.class.equals(type)
            || short.class.equals(type) || Short.class.equals(type)
            || int.class.equals(type) || Integer.class.equals(type)
            || long.class.equals(type) || Long.class.equals(type)
            || float.class.equals(type) || Float.class.equals(type)
            || double.class.equals(type) || Double.class.equals(type)
            || BigDecimal.class.equals(type) || BigInteger.class.equals(type);
    }

    /** The fields that a counting flag can store its count in. */
    public static boolean isCountingType(final Class<?> type) {
        return int.class.equals(type) || Integer.class.equals(type)
            || long.class.equals(type) || Long.class.equals(type);
    }

    // Lists that are raw, or that use a wildcard, have no parameter type: fall back to String, the same
    // way the parsing of positional lists does, instead of blowing up with a ClassCastException
    public static Class<?> toParameterType(final Field field) {
        return getTypeOfGenericField(field, String.class);
    }

    public static <T> void initializeLists(final T instance) throws IllegalAccessException {
        for (final Field field : listFields(instance.getClass())) {
            if (!isOptionOrPositional(field)) continue;
            if (!isListType(field)) continue;

            makeAccessible(field);

            field.set(instance, new ArrayList<>());
        }
    }

    private static boolean isOptionOrPositional(final Field field) {
        return field.isAnnotationPresent(CliOption.class) || field.isAnnotationPresent(CliPositional.class);
    }

    public static Class<?> getTypeOfGenericField(final Field field, final Class<?> defaultValue) {
        final Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) return defaultValue;
        final ParameterizedType paramType = (ParameterizedType) genericType;
        final Type[] typeArguments = paramType.getActualTypeArguments();
        if (typeArguments == null || typeArguments.length != 1) return defaultValue;
        if (!(typeArguments[0] instanceof Class)) return defaultValue;
        return (Class<?>) typeArguments[0];
    }

}
