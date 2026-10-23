package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestShortOptions {

    public static class Args {
        @CliOption(name = 'f', longName = "file")
        public String file;
        @CliOption(name = 'v', longName = "verbose")
        public boolean verbose;
        @CliOption(name = 'q', longName = "quiet")
        public boolean quiet;
    }

    @Test
    public void attachedShortForm() throws Exception {
        final String[] args = { "-fdata.txt" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void attachedShortFormWithEquals() throws Exception {
        final String[] args = { "-f=data.txt" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void groupedFlags() throws Exception {
        final String[] args = { "-vq" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertTrue(arguments.verbose, "The verbose flag should be set");
        assertTrue(arguments.quiet, "The quiet flag should be set");
        assertNull(arguments.file, "The file argument should be untouched");
    }

    @Test
    public void groupedFlagsWithValueAtTheEnd() throws Exception {
        final String[] args = { "-vf", "data.txt" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertTrue(arguments.verbose, "The verbose flag should be set");
        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void groupedFlagsWithAttachedValue() throws Exception {
        final String[] args = { "-qfdata.txt" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertTrue(arguments.quiet, "The quiet flag should be set");
        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void valueOptionEatsTheRestOfTheToken() throws Exception {
        final String[] args = { "-fv" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertEquals("v", arguments.file, "v is the value of the file option, not a flag");
        assertFalse(arguments.verbose, "The verbose flag should not be set");
    }

    @Test
    public void unknownCharacterInGroup() {
        final String[] args = { "-vz" };
        try {
            parseCommandLineArguments(args, Args::new);
            fail("Parsing should have failed on the unknown option");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            // a single character typo gets no suggestion, every letter is one edit away from every other
            assertEquals("'-z' is not a valid command line argument", e.getMessage());
        }
    }

    @Test
    public void singleDashLongFormSuggestsTheLongForm() {
        final String[] args = { "-file", "data.txt" };
        try {
            parseCommandLineArguments(args, Args::new);
            fail("Parsing should have failed on the single dash long form");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("Long form arguments must start with two dashes, '-file' does not. Did you mean '--file'?"
                    , e.getMessage());
        }
    }

    @Test
    public void groupWithAttachedEqualsOnAFlag() {
        final String[] args = { "-vq=true" };
        try {
            parseCommandLineArguments(args, Args::new);
            fail("Parsing should have failed, flags have no value");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("Flag arguments do not use attached long form", e.getMessage());
        }
    }

}
