package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestShortAndLongName {

    public static class Arguments {
        @CliOption(name = 'f', longName = "file", isMandatory = true)
        public String file;
    }

    @Test
    public void optionWithShortAndLong() throws InvalidCommandLine {
        final String[] args = { "-f", "boe"};
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("File was set with the correct value", "boe", arguments.file);
    }

    @Test(expected = InvalidCommandLine.class)
    public void optionWithMissingArgument() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Arguments::new);

        fail("The code failed to throw the MissingArgument exception");
    }
}
