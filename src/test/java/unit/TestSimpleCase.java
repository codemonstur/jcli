package unit;

import jcli.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.*;

public class TestSimpleCase {

    public static class SingleDash {
        @CliOption(isMandatory = true, name = 'f')
        public String file;
    }

    public static class DoubleDash {
        @CliOption(isMandatory = true, longName = "file")
        public String file;
    }

    public static class Flag {
        @CliOption(name = 'f')
        public boolean file;
    }

    @Test
    public void happyCaseSingleDash() throws InvalidCommandLine {
        final String[] args = { "-f", "file.txt" };
        final SingleDash arguments = parseCommandLineArguments(args, SingleDash::new);

        assertEquals("The file argument is not equal", "file.txt", arguments.file);
    }

    @Test
    public void happyCaseDoubleDash() throws InvalidCommandLine {
        final String[] args = { "--file", "file.txt" };
        final DoubleDash arguments = parseCommandLineArguments(args, DoubleDash::new);

        assertEquals("The file argument is not equal", "file.txt", arguments.file);
    }

    @Test
    public void happyCaseFlag() throws InvalidCommandLine {
        final Flag set = parseCommandLineArguments(new String[] { "-f" }, Flag::new);
        final Flag not = parseCommandLineArguments(new String[] {}, Flag::new);

        assertTrue("The file argument is not equal", set.file);
        assertFalse("The file argument is not equal", not.file);
    }

}
