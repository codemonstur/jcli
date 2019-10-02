package unit;

import jcli.CliOption;
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

    public static class Optionals {
        @CliOption(name = 'l', defaultValue = "")
        public OptionalLong optionalLong;
        @CliOption(name = 'i', defaultValue = "")
        public OptionalInt optionalInt;
        @CliOption(name = 'd', defaultValue = "")
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
        final Optionals arguments = parseCommandLineArguments(args, Optionals::new);

        assertFalse("Double is not set correctly", arguments.optionalDouble.isPresent());
        assertFalse("Long is not set correctly", arguments.optionalLong.isPresent());
        assertFalse("Int is not set correctly", arguments.optionalInt.isPresent());
    }

}
