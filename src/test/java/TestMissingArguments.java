import jcli.Argument;
import jcli.errors.*;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestMissingArguments {

    public static class Arguments {
        @Argument(mandatory = true, longName = "file")
        public final String file;

        public Arguments(final String file) {
            this.file = file;
        }
    }

    @Test(expected = UnknownCommandLineOption.class)
    public void parseUnknownCommandLineOption() throws InvalidArgumentConfiguration, InvalidCommandLine {
        final String[] args = { "--something", "file.txt" };
        parseCommandLineArguments(args, Arguments.class);

        fail("Parser failed to throw UnknownCommandLineOption");
    }

    @Test(expected = MissingArgument.class)
    public void parseMissingArgument() throws InvalidArgumentConfiguration, InvalidCommandLine {
        final String[] args = { };
        parseCommandLineArguments(args, Arguments.class);

        fail("Parser failed to throw MissingArgument");
    }

    @Test(expected = MissingCommandLineValue.class)
    public void parseUnknownCommandLineValue() throws InvalidArgumentConfiguration, InvalidCommandLine {
        final String[] args = { "--file" };
        parseCommandLineArguments(args, Arguments.class);

        fail("Parser failed to throw MissingCommandLineValue");
    }
}
