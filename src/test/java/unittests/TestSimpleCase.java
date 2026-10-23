package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

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

    public static class Final {
        @CliOption(isMandatory = true, longName = "file")
        public final String file;
        @CliPositional
        public final String positional;

        public Final() {
            file = null;
            positional = null;
        }
    }

    @Test
    public void happyCaseSingleDash() throws InvalidCommandLine {
        final String[] args = { "-f", "file.txt" };
        final SingleDash arguments = parseCommandLineArguments(args, SingleDash::new);

        assertEquals("file.txt", arguments.file, "The file argument is not equal");
    }

    @Test
    public void happyCaseDoubleDash() throws InvalidCommandLine {
        final String[] args = { "--file", "file.txt" };
        final DoubleDash arguments = parseCommandLineArguments(args, DoubleDash::new);

        assertEquals("file.txt", arguments.file, "The file argument is not equal");
    }

    @Test
    public void happyCaseFlag() throws InvalidCommandLine {
        final Flag set = parseCommandLineArguments(new String[] { "-f" }, Flag::new);
        final Flag not = parseCommandLineArguments(new String[] {}, Flag::new);

        assertTrue(set.file, "The file argument is not equal");
        assertFalse(not.file, "The file argument is not equal");
    }

    @Test
    public void happyCaseFinalFields() throws InvalidCommandLine {
        final String[] args = { "--file", "file.txt", "some-positional" };
        final Final arguments = parseCommandLineArguments(args, Final::new);

        assertEquals("file.txt", arguments.file, "The file argument is not equal");
        assertEquals("some-positional", arguments.positional, "The positional is not equal");
    }

}
