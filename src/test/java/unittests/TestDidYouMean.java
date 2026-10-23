package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestDidYouMean {

    public static class Args {
        @CliOption(longName = "file")
        public String file;
        @CliOption(longName = "output")
        public String output;
    }

    @Test
    public void suggestsTheClosestOption() {
        final String[] args = { "--flie", "data.txt" };
        try {
            parseCommandLineArguments(args, Args::new);
            fail("Parsing should have failed on the typo");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("'--flie' is not a valid command line argument. Did you mean '--file'?", e.getMessage());
        }
    }

    @Test
    public void suggestsNothingWhenNothingIsClose() {
        final String[] args = { "--zzzzzzzz", "data.txt" };
        try {
            parseCommandLineArguments(args, Args::new);
            fail("Parsing should have failed on the unknown option");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("'--zzzzzzzz' is not a valid command line argument", e.getMessage());
        }
    }

    @Test
    public void suggestsAShortOptionForATypoInAShortName() {
        final String[] args = { "-x" };
        try {
            parseCommandLineArguments(args, Args::new);
            fail("Parsing should have failed on the unknown option");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("'-x' is not a valid command line argument", e.getMessage());
        }
    }

}
