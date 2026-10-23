package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestValueErrors {

    public static class Count {
        @CliOption(longName = "count")
        public int count;
    }

    public static class Files {
        @CliOption(longName = "files")
        public List<Integer> files;
    }

    public static class Offset {
        @CliPositional
        public int offset;
    }

    @Test
    public void badValueNamesTheOptionAndTheType() {
        final String[] args = { "--count", "abc" };
        try {
            parseCommandLineArguments(args, Count::new);
            fail("Parsing should have failed on the wrong format");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("The value 'abc' for '--count' is not a valid int", e.getMessage());
        }
    }

    @Test
    public void badListElementNamesTheOption() {
        final String[] args = { "--files", "abc" };
        try {
            parseCommandLineArguments(args, Files::new);
            fail("Parsing should have failed on the wrong format");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("The value 'abc' for '--files' is not a valid Integer", e.getMessage());
        }
    }

    @Test
    public void badPositionalNamesTheField() {
        final String[] args = { "abc" };
        try {
            parseCommandLineArguments(args, Offset::new);
            fail("Parsing should have failed on the wrong format");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("The value 'abc' for 'offset' is not a valid int", e.getMessage());
        }
    }

}
