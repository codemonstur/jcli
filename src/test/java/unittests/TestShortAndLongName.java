package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestShortAndLongName {

    public static class Arguments {
        @CliOption(name = 'f', longName = "file", isMandatory = true)
        public String file;
    }

    @Test
    public void optionWithShortAndLong() throws InvalidCommandLine {
        final String[] args = { "-f", "boe"};
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("boe", arguments.file, "File was set with the correct value");
    }

    @Test
    public void optionWithMissingArgument() {
        final String[] args = {};

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Arguments::new));
    }
}
