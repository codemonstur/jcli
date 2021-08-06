package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestOptional {

    public static class Arguments {
        @CliOption(name = 'f', defaultValue ="default")
        public String file;
    }

    public static class WithDefaults {
        @CliOption(name = 'l', defaultValue = "")
        public OptionalLong optionalLong;
        @CliOption(name = 'i', defaultValue = "")
        public OptionalInt optionalInt;
        @CliOption(name = 'd', defaultValue = "")
        public OptionalDouble optionalDouble;
    }

    public static class WithoutDefaults {
        @CliOption(name = 'l')
        public OptionalLong optionalLong;
        @CliOption(name = 'i')
        public OptionalInt optionalInt;
        @CliOption(name = 'd')
        public OptionalDouble optionalDouble;
    }

    @Test
    public void defaultIsSet() throws InvalidCommandLine {
        final String[] args = { };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The file argument is not equal", "default", arguments.file);
    }

    @Test
    public void defaultIsIgnored() throws InvalidCommandLine {
        final String[] args = { "-f", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The hello argument is not equal", "file.txt", arguments.file);
    }

    @Test
    public void optionalsAreEmpty() throws InvalidCommandLine {
        final String[] args = {};
        final WithDefaults arguments = parseCommandLineArguments(args, WithDefaults::new);

        assertFalse("Double is not set correctly", arguments.optionalDouble.isPresent());
        assertFalse("Long is not set correctly", arguments.optionalLong.isPresent());
        assertFalse("Int is not set correctly", arguments.optionalInt.isPresent());
    }

    @Test
    public void optionalsWithoutDefaultsAreEmpty() throws InvalidCommandLine {
        final String[] args = {};
        final WithoutDefaults arguments = parseCommandLineArguments(args, WithoutDefaults::new);

        assertFalse("Double is not set correctly", arguments.optionalDouble.isPresent());
        assertFalse("Long is not set correctly", arguments.optionalLong.isPresent());
        assertFalse("Int is not set correctly", arguments.optionalInt.isPresent());
    }

}
