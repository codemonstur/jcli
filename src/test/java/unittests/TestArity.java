package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestArity {

    public static class Force {
        @CliOption(longName = "force", arity = 1)
        public Boolean force;
    }

    public static class Pair {
        @CliOption(longName = "pair", arity = 2)
        public List<String> pair;
    }

    public static class Greedy {
        @CliOption(longName = "files", arity = -1)
        public List<String> files;
        @CliOption(name = 'v', longName = "verbose")
        public boolean verbose;
    }

    public static class ArityOnString {
        @CliOption(longName = "file", arity = 3)
        public String file;
    }

    public static class ArityOnHelp {
        @CliOption(name = 'h', longName = "help", isHelp = true, arity = 1)
        public boolean help;
    }

    @Test
    public void booleanWithExplicitTrue() throws Exception {
        final Force arguments = parseCommandLineArguments(new String[]{"--force=true"}, Force::new);

        assertEquals(Boolean.TRUE, arguments.force);
    }

    @Test
    public void booleanWithExplicitFalse() throws Exception {
        final Force arguments = parseCommandLineArguments(new String[]{"--force=false"}, Force::new);

        assertEquals(Boolean.FALSE, arguments.force);
    }

    @Test
    public void booleanValueFromTheNextToken() throws Exception {
        final Force arguments = parseCommandLineArguments(new String[]{"--force", "true"}, Force::new);

        assertEquals(Boolean.TRUE, arguments.force);
    }

    @Test
    public void booleanWithoutTheOptionIsNull() throws Exception {
        final Force arguments = parseCommandLineArguments(new String[]{}, Force::new);

        assertNull(arguments.force, "A value taking Boolean that is not given stays null");
    }

    @Test
    public void booleanRejectsOtherValues() {
        final String[] args = { "--force", "yes" };
        try {
            parseCommandLineArguments(args, Force::new);
            fail("Parsing should have failed on a value that is not true or false");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("The value 'yes' for '--force' must be true or false", e.getMessage());
        }
    }

    @Test
    public void booleanWithoutAValue() {
        final String[] args = { "--force" };
        try {
            parseCommandLineArguments(args, Force::new);
            fail("Parsing should have failed on the missing value");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("Argument '--force' is missing a value", e.getMessage());
        }
    }

    @Test
    public void pairConsumesTwoValues() throws Exception {
        final Pair arguments = parseCommandLineArguments(new String[]{"--pair", "a", "b"}, Pair::new);

        assertEquals(List.of("a", "b"), arguments.pair);
    }

    @Test
    public void pairInAttachedForm() throws Exception {
        final Pair arguments = parseCommandLineArguments(new String[]{"--pair=a,b"}, Pair::new);

        assertEquals(List.of("a", "b"), arguments.pair);
    }

    @Test
    public void pairWithTooFewValues() {
        final String[] args = { "--pair", "a" };
        try {
            parseCommandLineArguments(args, Pair::new);
            fail("Parsing should have failed on the missing value");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("The option '--pair' expects 2 values but only 1 were given", e.getMessage());
        }
    }

    @Test
    public void greedyConsumesUntilTheNextOption() throws Exception {
        final Greedy arguments = parseCommandLineArguments(new String[]{"--files", "a", "b", "-v"}, Greedy::new);

        assertEquals(List.of("a", "b"), arguments.files);
        assertTrue(arguments.verbose, "The verbose flag should be set");
    }

    @Test
    public void greedyAsTheLastOption() throws Exception {
        final Greedy arguments = parseCommandLineArguments(new String[]{"-v", "--files", "a", "b"}, Greedy::new);

        assertEquals(List.of("a", "b"), arguments.files);
        assertTrue(arguments.verbose, "The verbose flag should be set");
    }

    @Test
    public void arityOnNonListIsAConfigurationError() {
        try {
            parseCommandLineArguments(new String[]{}, ArityOnString::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("The field 'file' declares an arity of 3 which requires a List field", e.getMessage());
        }
    }

    @Test
    public void arityOnHelpIsAConfigurationError() {
        try {
            parseCommandLineArguments(new String[]{}, ArityOnHelp::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("The field 'help' is a help or version flag and cannot declare an arity", e.getMessage());
        }
    }

}
