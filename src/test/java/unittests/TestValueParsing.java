package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

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
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("Argument '--file' is missing a value", e.getMessage());
        }
    }

    @Test
    public void negativeNumberAsOptionValue() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "--count", "-5" };
        final Count arguments = parseCommandLineArguments(args, Count::new);

        assertEquals(-5, arguments.count, "The count argument is not equal");
    }

    @Test
    public void negativeNumberInAttachedForm() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "--count=-5" };
        final Count arguments = parseCommandLineArguments(args, Count::new);

        assertEquals(-5, arguments.count, "The count argument is not equal");
    }

    @Test
    public void negativeNumberAsPositional() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "-5" };
        final Offset arguments = parseCommandLineArguments(args, Offset::new);

        assertEquals(-5, arguments.offset, "The offset argument is not equal");
    }

    @Test
    public void doubleDashEndsTheOptions() throws InvalidCommandLine, InvalidOptionConfiguration {
        final String[] args = { "--file", "file.txt", "--", "-h" };
        final FileAndAction arguments = parseCommandLineArguments(args, FileAndAction::new);

        assertEquals("file.txt", arguments.file, "The file argument is not equal");
        assertFalse(arguments.help, "The help flag should not be set");
        assertEquals("-h", arguments.action, "The action argument is not equal");
    }

}
