import jcli.CliOption;
import jcli.errors.*;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestMissingArguments {

    public static class Arguments {
        @CliOption(mandatory = true, longName = "file")
        public String file;
    }

    @Test(expected = UnknownCommandLineOption.class)
    public void parseUnknownCommandLineOption() throws InvalidCommandLine {
        final String[] args = { "--something", "file.txt" };
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw UnknownCommandLineOption");
    }

    @Test(expected = MissingArgument.class)
    public void parseMissingArgument() throws InvalidCommandLine {
        final String[] args = { };
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw MissingArgument");
    }

    @Test(expected = MissingCommandLineValue.class)
    public void parseUnknownCommandLineValue() throws InvalidCommandLine {
        final String[] args = { "--file" };
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw MissingCommandLineValue");
    }
}
