package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.*;

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

        assertTrue("The verbose flag should be set", arguments.verbose);
        assertTrue("The quiet flag should be set", arguments.quiet);
        assertNull("The file argument should be untouched", arguments.file);
    }

    @Test
    public void groupedFlagsWithValueAtTheEnd() throws Exception {
        final String[] args = { "-vf", "data.txt" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertTrue("The verbose flag should be set", arguments.verbose);
        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void groupedFlagsWithAttachedValue() throws Exception {
        final String[] args = { "-qfdata.txt" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertTrue("The quiet flag should be set", arguments.quiet);
        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void valueOptionEatsTheRestOfTheToken() throws Exception {
        final String[] args = { "-fv" };
        final Args arguments = parseCommandLineArguments(args, Args::new);

        assertEquals("v is the value of the file option, not a flag", "v", arguments.file);
        assertFalse("The verbose flag should not be set", arguments.verbose);
    }

    @Test
    public void unknownCharacterInGroup() {
        final String[] args = { "-vz" };
        try {
            parseCommandLineArguments(args, Args::new);
            fail("Parsing should have failed on the unknown option");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
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
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
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
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("Flag arguments do not use attached long form", e.getMessage());
        }
    }

}
