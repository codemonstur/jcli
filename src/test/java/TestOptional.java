import jcli.Argument;
import jcli.errors.InvalidArgumentConfiguration;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestOptional {

    public static class Arguments {
        @Argument(name = 'f', defaultValue ="default")
        public final String file;

        public Arguments(final String file) {
            this.file = file;
        }
    }

    @Test
    public void parseOptionalMissing() throws InvalidArgumentConfiguration, InvalidCommandLine {
        final String[] args = { "--not-f", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments.class);

        assertEquals("The file argument is not equal", "default", arguments.file);
    }

    @Test
    public void parseOptionalExists() throws InvalidArgumentConfiguration, InvalidCommandLine {
        final String[] args = { "-f", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments.class);

        assertEquals("The hello argument is not equal", "file.txt", arguments.file);
    }

}
