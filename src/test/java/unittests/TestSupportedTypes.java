package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestSupportedTypes {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class AllSupportedTypes {
        @CliOption(isMandatory = true, longName = "string")
        private String string;
        @CliOption(isMandatory = true, longName = "doubleClass")
        private Double doubleClass;
        @CliOption(isMandatory = true, longName = "doublePrimitive")
        private double doublePrimitive;
        @CliOption(isMandatory = true, longName = "intClass")
        private Integer intClass;
        @CliOption(isMandatory = true, longName = "intPrimitive")
        private int intPrimitive;
        @CliOption(isMandatory = true, longName = "floatClass")
        private Float floatClass;
        @CliOption(isMandatory = true, longName = "floatPrimitive")
        private float floatPrimitive;
        @CliOption(isMandatory = true, longName = "byteClass")
        private Byte byteClass;
        @CliOption(isMandatory = true, longName = "bytePrimitive")
        private byte bytePrimitive;
        @CliOption(isMandatory = true, longName = "charClass")
        private Character charClass;
        @CliOption(isMandatory = true, longName = "charPrimitive")
        private char charPrimitive;
        @CliOption(isMandatory = true, longName = "shortClass")
        private Short shortClass;
        @CliOption(isMandatory = true, longName = "shortPrimitive")
        private short shortPrimitive;
        @CliOption(isMandatory = true, longName = "longClass")
        private Long longClass;
        @CliOption(isMandatory = true, longName = "longPrimitive")
        private long longPrimitive;
        @CliOption(isMandatory = true, longName = "booleanClass")
        private Boolean booleanClass;
        @CliOption(isMandatory = true, longName = "booleanPrimitive")
        private boolean booleanPrimitive;
        @CliOption(isMandatory = true, longName = "file")
        private File file;
        @CliOption(isMandatory = true, longName = "bigDecimal")
        private BigDecimal bigDecimal;
        @CliOption(isMandatory = true, longName = "bigInteger")
        private BigInteger bigInteger;
        @CliOption(isMandatory = true, longName = "uri")
        private URI uri;
        @CliOption(isMandatory = true, longName = "path")
        private Path path;
        @CliOption(isMandatory = true, longName = "instant")
        private Instant instant;
        @CliOption(isMandatory = true, longName = "localTime")
        private LocalTime localTime;
        @CliOption(isMandatory = true, longName = "localDate")
        private LocalDate localDate;
        @CliOption(isMandatory = true, longName = "localDateTime")
        private LocalDateTime localDateTime;
        @CliOption(isMandatory = true, longName = "offsetDateTime")
        private OffsetDateTime offsetDateTime;
        @CliOption(isMandatory = true, longName = "zonedDateTime")
        private ZonedDateTime zonedDateTime;
        @CliOption(isMandatory = true, longName = "optionalDouble")
        private OptionalDouble optionalDouble;
        @CliOption(isMandatory = true, longName = "optionalInt")
        private OptionalInt optionalInt;
        @CliOption(isMandatory = true, longName = "optionalLong")
        private OptionalLong optionalLong;
        @CliOption(isMandatory = true, longName = "pattern")
        private Pattern pattern;
        @CliOption(isMandatory = true, longName = "charset")
        private Charset charset;
        @CliOption(isMandatory = true, longName = "enum")
        private TestEnum enumValue;
    }

    private enum TestEnum {
        test, something
    }

    @Test
    public void happyCaseSupportedTypes() throws InvalidCommandLine {
        final String[] args = { "--string", "string", "--doubleClass", "1.0", "--doublePrimitive", "1.0",
            "--intClass", "1", "--intPrimitive", "1", "--floatClass", "1.0", "--floatPrimitive", "1.0",
            "--byteClass", "1", "--bytePrimitive", "1", "--charClass", "a", "--charPrimitive", "a",
            "--shortClass", "1", "--shortPrimitive", "1", "--longClass", "1", "--longPrimitive", "1",
            "--booleanClass", "--booleanPrimitive", "--file", ".", "--bigDecimal", "1",
            "--bigInteger", "1", "--uri", "http://www.google.com", "--path", "..", "--instant", "2019-10-02T10:23:54.735Z",
            "--localTime", "12:24:39.562", "--localDate", "2019-10-02", "--localDateTime", "2019-10-02T12:25:27.081",
            "--offsetDateTime", "2019-10-02T12:25:49.005+02:00", "--zonedDateTime", "2019-10-02T12:26:10.461+02:00[Europe/Amsterdam]",
            "--optionalDouble", "1.0", "--optionalInt", "1", "--optionalLong", "1", "--pattern", ".*",
            "--charset", "UTF-8", "--enum", "test"};
        final AllSupportedTypes arguments = parseCommandLineArguments(args, AllSupportedTypes::new);

        assertNotNull(arguments.bigDecimal, format("The %s argument was not set", "bigDecimal"));
        assertNotNull(arguments.bigInteger, format("The %s argument was not set", "bigInteger"));
        assertNotNull(arguments.booleanClass, format("The %s argument was not set", "booleanClass"));
        assertNotNull(arguments.byteClass, format("The %s argument was not set", "byteClass"));
        assertNotNull(arguments.charClass, format("The %s argument was not set", "charClass"));
        assertNotNull(arguments.charset, format("The %s argument was not set", "charset"));
        assertNotNull(arguments.doubleClass, format("The %s argument was not set", "doubleClass"));
        assertNotNull(arguments.file, format("The %s argument was not set", "file"));
        assertNotNull(arguments.floatClass, format("The %s argument was not set", "floatClass"));
        assertNotNull(arguments.instant, format("The %s argument was not set", "instant"));
        assertNotNull(arguments.intClass, format("The %s argument was not set", "intClass"));
        assertNotNull(arguments.localDate, format("The %s argument was not set", "localDate"));
        assertNotNull(arguments.localDateTime, format("The %s argument was not set", "localDateTime"));
        assertNotNull(arguments.localTime, format("The %s argument was not set", "localTime"));
        assertNotNull(arguments.longClass, format("The %s argument was not set", "longClass"));
        assertNotNull(arguments.offsetDateTime, format("The %s argument was not set", "offsetDateTime"));
        assertNotNull(arguments.optionalDouble, format("The %s argument was not set", "optionalDouble"));
        assertNotNull(arguments.optionalInt, format("The %s argument was not set", "optionalInt"));
        assertNotNull(arguments.optionalLong, format("The %s argument was not set", "optionalLong"));
        assertNotNull(arguments.path, format("The %s argument was not set", "path"));
        assertNotNull(arguments.pattern, format("The %s argument was not set", "pattern"));
        assertNotNull(arguments.shortClass, format("The %s argument was not set", "shortClass"));
        assertNotNull(arguments.string, format("The %s argument was not set", "string"));
        assertNotNull(arguments.uri, format("The %s argument was not set", "uri"));
        assertNotNull(arguments.zonedDateTime, format("The %s argument was not set", "zonedDateTime"));
        assertEquals(1, arguments.bytePrimitive, format("The %s argument was not set", "bytePrimitive"));
        assertEquals('a', arguments.charPrimitive, format("The %s argument was not set", "charPrimitive"));
        assertEquals(1, arguments.intPrimitive, format("The %s argument was not set", "intPrimitive"));
        assertEquals(1, arguments.shortPrimitive, format("The %s argument was not set", "shortPrimitive"));
        assertEquals(1.0, arguments.doublePrimitive, 0.0000001, format("The %s argument was not set", "doublePrimitive"));
        assertEquals(1.0f, arguments.floatPrimitive, 0.0000001f, format("The %s argument was not set", "floatPrimitive"));
        assertEquals(1, arguments.longPrimitive, format("The %s argument was not set", "longPrimitive"));
        assertEquals(TestEnum.test, arguments.enumValue, format("The %s argument was not set", "enumValue"));
        assertTrue(arguments.booleanPrimitive, format("The %s argument was not set", "booleanPrimitive"));
    }

    public static class WrongFieldType {
        @CliOption(longName = "file")
        public Set file;
    }

    @Test
    public void optionWithInvalidType() {
        final String[] args = {};

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, WrongFieldType::new));
    }

}
