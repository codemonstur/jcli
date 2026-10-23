package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestOptional {

    public static class Arguments {
        @CliOption(name = 'f', defaultValue = "default")
        public String file;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class WithDefaults {
        @CliOption(name = 'l', defaultValue = "")
        public OptionalLong optionalLong;
        @CliOption(name = 'i', defaultValue = "")
        public OptionalInt optionalInt;
        @CliOption(name = 'd', defaultValue = "")
        public OptionalDouble optionalDouble;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class WithoutDefaults {
        @CliOption(name = 'l')
        public OptionalLong optionalLong;
        @CliOption(name = 'i')
        public OptionalInt optionalInt;
        @CliOption(name = 'd')
        public OptionalDouble optionalDouble;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class TypedOptional {
        @CliOption(name = 's')
        public Optional<String> optionalString;
        @CliOption(name = 'i')
        public Optional<Integer> optionalInteger;
    }

    @Test
    public void defaultIsSet() throws InvalidCommandLine {
        final String[] args = { };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("default", arguments.file, "The file argument is not equal");
    }

    @Test
    public void defaultIsIgnored() throws InvalidCommandLine {
        final String[] args = { "-f", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("file.txt", arguments.file, "The hello argument is not equal");
    }

    @Test
    public void optionalsAreEmpty() throws InvalidCommandLine {
        final String[] args = {};
        final WithDefaults arguments = parseCommandLineArguments(args, WithDefaults::new);

        assertFalse(arguments.optionalDouble.isPresent(), "Double is not set correctly");
        assertFalse(arguments.optionalLong.isPresent(), "Long is not set correctly");
        assertFalse(arguments.optionalInt.isPresent(), "Int is not set correctly");
    }

    @Test
    public void optionalsWithoutDefaultsAreEmpty() throws InvalidCommandLine {
        final String[] args = {};
        final WithoutDefaults arguments = parseCommandLineArguments(args, WithoutDefaults::new);

        assertFalse(arguments.optionalDouble.isPresent(), "Double is not set correctly");
        assertFalse(arguments.optionalLong.isPresent(), "Long is not set correctly");
        assertFalse(arguments.optionalInt.isPresent(), "Int is not set correctly");
    }

    @Test
    public void typedOptionalIsSetWhenEmpty() throws InvalidCommandLine {
        final String[] args = {};
        final TypedOptional arguments = parseCommandLineArguments(args, TypedOptional::new);

        assertFalse(arguments.optionalString.isPresent(), "String is not set correctly");
        assertFalse(arguments.optionalInteger.isPresent(), "Integer is not set correctly");
    }

    @Test
    public void typedOptionalIsSetWithValue() throws InvalidCommandLine {
        final String[] args = { "-s", "hello", "-i", "5" };
        final TypedOptional arguments = parseCommandLineArguments(args, TypedOptional::new);

        assertEquals("hello", arguments.optionalString.get(), "String is not set correctly");
        assertEquals(Integer.valueOf(5), arguments.optionalInteger.get(), "Integer is not set correctly");
    }

}
