import jcli.CliOption;
import jcli.errors.InvalidOptionConfiguration;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestSimpleCase {

    public static class Arguments {
        @CliOption(mandatory = true, longName = "file")
        public final String file;

        public Arguments() {
            this.file = null;
        }
    }

    @Test
    public void parseSimple() throws InvalidOptionConfiguration, InvalidCommandLine {
        final String[] args = { "--file", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The file argument is not equal", "file.txt", arguments.file);
    }

}
