package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestErrorMessages {

    public static class ShortNameOnly {
        @CliOption(name = 'f', isMandatory = true)
        public String file;
    }

    public static class LongNameOnly {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
    }

    public static class OptionalArgument {
        @CliOption(longName = "file")
        public String file;
    }

    @Test
    public void missingArgumentNamesTheShortOption() {
        try {
            final String[] args = { };
            parseCommandLineArguments(args, ShortNameOnly::new);
            fail("Parsing should have failed with a missing argument");
        } catch (final Exception e) {
            assertEquals("Missing argument '-f'", e.getMessage());
        }
    }

    @Test
    public void missingArgumentNamesTheLongOption() {
        try {
            final String[] args = { };
            parseCommandLineArguments(args, LongNameOnly::new);
            fail("Parsing should have failed with a missing argument");
        } catch (final Exception e) {
            assertEquals("Missing argument '--file'", e.getMessage());
        }
    }

    @Test
    public void repeatedArgumentIsReportedAsDuplicate() {
        try {
            final String[] args = { "--file", "one", "--file", "two" };
            parseCommandLineArguments(args, OptionalArgument::new);
            fail("Parsing should have failed with a duplicate argument");
        } catch (final Exception e) {
            assertEquals("'--file' was provided more than once", e.getMessage());
        }
    }

    @Test
    public void repeatedShortArgumentIsReportedAsDuplicate() {
        try {
            final String[] args = { "-f", "one", "-f", "two" };
            parseCommandLineArguments(args, ShortNameOnly::new);
            fail("Parsing should have failed with a duplicate argument");
        } catch (final InvalidCommandLine e) {
            assertEquals("'-f' was provided more than once", e.getMessage());
        } catch (final InvalidOptionConfiguration e) {
            fail("Wrong exception thrown: " + e.getMessage());
        }
    }

}
