package unit;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import jcli.errors.TooManyPositionalArguments;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestPositional {

    public static class Arguments {
        @CliOption(name = 'l', defaultValue = "1")
        public long l;
        @CliPositional(index = 0, defaultValue = "file.txt")
        public String file;
    }

    @Test
    public void setPositionalFromCli() throws InvalidCommandLine {
        final String[] args = { "-l", "2", "somefile.txt"};
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The l argument is not equal", 2, arguments.l);
        assertEquals("The file argument is not equal", "somefile.txt", arguments.file);
    }

    @Test
    public void defaultForPositional() throws InvalidCommandLine {
        final String[] args = { };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("The l argument is not equal", 1, arguments.l);
        assertEquals("The file argument is not equal", "file.txt", arguments.file);
    }

    @Test(expected = TooManyPositionalArguments.class)
    public void tooManyPositionalArguments() throws InvalidCommandLine {
        final String[] args = { "hello", "one-too-many" };
        parseCommandLineArguments(args, Arguments::new);

        fail("TooManyPositionalArguments not thrown");
    }

}
