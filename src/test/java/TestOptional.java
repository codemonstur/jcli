import jcli.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestOptional {

    public static class Arguments {
        @CliOption(name = 'f', defaultValue ="default")
        public String file;
    }

    @Test
    public void parseOptionalMissing() throws InvalidCommandLine {
        final String[] args = { };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The file argument is not equal", "default", arguments.file);
    }

    @Test
    public void parseOptionalExists() throws InvalidCommandLine {
        final String[] args = { "-f", "file.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The hello argument is not equal", "file.txt", arguments.file);
    }

}
