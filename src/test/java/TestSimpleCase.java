import jcli.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestSimpleCase {

    public static class Arguments {
        @CliOption(mandatory = true, longName = "file")
        public String file;
    }

    @Test
    public void parseSimple() throws InvalidCommandLine {
        final String[] args = { "--file", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The file argument is not equal", "file.txt", arguments.file);
    }

}
