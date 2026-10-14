package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.*;

public class TestValueParsing {

    public static class FileAndHelp {
        @CliOption(longName = "file")
        public String file;
        @CliOption(name = 'h', isHelp = true)
        public boolean help;
    }

    public static class Count {
        @CliOption(longName = "count")
        public int count;
    }

    public static class Offset {
        @CliPositional
        public int offset;
    }

    public static class FileAndAction {
        @CliOption(longName = "file")
        public String file;
        @CliOption(name = 'h', isHelp = true)
        public boolean help;
        @CliPositional
        public String action;
    }

    @Test
    public void valueThatLooksLikeAnOptionIsMissing() {
        final String[] args = { "--file", "-h" };
        try {
            parseCommandLineArguments(args, FileAndHelp::new);
            fail("Parsing should have failed with a missing value");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("Argument '--file' is missing a value", e.getMessage());
        }
    }

    @Test
    public void negativeNumberAsOptionValue() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "--count", "-5" };
        final Count arguments = parseCommandLineArguments(args, Count::new);

        assertEquals("The count argument is not equal", -5, arguments.count);
    }

    @Test
    public void negativeNumberInAttachedForm() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "--count=-5" };
        final Count arguments = parseCommandLineArguments(args, Count::new);

        assertEquals("The count argument is not equal", -5, arguments.count);
    }

    @Test
    public void negativeNumberAsPositional() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "-5" };
        final Offset arguments = parseCommandLineArguments(args, Offset::new);

        assertEquals("The offset argument is not equal", -5, arguments.offset);
    }

    @Test
    public void doubleDashEndsTheOptions() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "--file", "file.txt", "--", "-h" };
        final FileAndAction arguments = parseCommandLineArguments(args, FileAndAction::new);

        assertEquals("The file argument is not equal", "file.txt", arguments.file);
        assertFalse("The help flag should not be set", arguments.help);
        assertEquals("The action argument is not equal", "-h", arguments.action);
    }

}
