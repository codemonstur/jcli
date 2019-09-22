import jcli.Argument;
import jcli.errors.InvalidArgumentConfiguration;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestSimpleCase {

    public static class Arguments {
        @Argument(mandatory = true, longName = "file")
        public final String file;

        public Arguments(final String file) {
            this.file = file;
        }
    }

    @Test
    public void parseSimple() throws InvalidArgumentConfiguration, InvalidCommandLine {
        final String[] args = { "--file", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments.class);

        assertEquals("The file argument is not equal", "file.txt", arguments.file);
    }

}
