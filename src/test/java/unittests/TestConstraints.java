package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.*;

public class TestConstraints {

    public static class Mode {
        @CliOption(longName = "mode", allowedValues = {"read", "write"})
        public String mode;
    }

    public static class Port {
        @CliOption(longName = "port", min = "1", max = "65535")
        public int port;
    }

    public static class Host {
        @CliOption(longName = "host", regex = "[a-z]+\\.[a-z]+")
        public String host;
    }

    public static class Related {
        @CliOption(longName = "verbose")
        public boolean verbose;
        @CliOption(longName = "log", requires = "verbose")
        public String log;
        @CliOption(longName = "quiet", excludes = "verbose")
        public boolean quiet;
    }

    public static class MinOnString {
        @CliOption(longName = "file", min = "1")
        public String file;
    }

    public static class BadRegex {
        @CliOption(longName = "file", regex = "[a-z")
        public String file;
    }

    public static class MinAboveMax {
        @CliOption(longName = "port", min = "10", max = "5")
        public int port;
    }

    public static class UnknownRelated {
        @CliOption(longName = "log", requires = "nope")
        public String log;
    }

    public static class HelpWins {
        @CliOption(longName = "help", isHelp = true)
        public boolean help;
        @CliOption(longName = "log", requires = "verbose")
        public String log;
        @CliOption(longName = "verbose")
        public boolean verbose;
    }

    @Test
    public void allowedValueIsAccepted() throws Exception {
        final Mode arguments = parseCommandLineArguments(new String[]{"--mode", "read"}, Mode::new);

        assertEquals("read", arguments.mode);
    }

    @Test
    public void disallowedValueIsRejected() {
        try {
            parseCommandLineArguments(new String[]{"--mode", "delete"}, Mode::new);
            fail("Parsing should have failed on the disallowed value");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("The value 'delete' for '--mode' is not allowed, it must be one of: read, write"
                    , e.getMessage());
        }
    }

    @Test
    public void valueInRangeIsAccepted() throws Exception {
        final Port arguments = parseCommandLineArguments(new String[]{"--port", "8080"}, Port::new);

        assertEquals(8080, arguments.port);
    }

    @Test
    public void valueTooSmallIsRejected() {
        try {
            parseCommandLineArguments(new String[]{"--port", "0"}, Port::new);
            fail("Parsing should have failed on the value that is too small");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("The value '0' for '--port' must be at least 1", e.getMessage());
        }
    }

    @Test
    public void valueTooLargeIsRejected() {
        try {
            parseCommandLineArguments(new String[]{"--port", "70000"}, Port::new);
            fail("Parsing should have failed on the value that is too large");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("The value '70000' for '--port' must be at most 65535", e.getMessage());
        }
    }

    @Test
    public void matchingValueIsAccepted() throws Exception {
        final Host arguments = parseCommandLineArguments(new String[]{"--host", "example.com"}, Host::new);

        assertEquals("example.com", arguments.host);
    }

    @Test
    public void nonMatchingValueIsRejected() {
        try {
            parseCommandLineArguments(new String[]{"--host", "Example"}, Host::new);
            fail("Parsing should have failed on the value that does not match");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("The value 'Example' for '--host' does not match the pattern [a-z]+\\.[a-z]+"
                    , e.getMessage());
        }
    }

    @Test
    public void requiredOptionIsPresent() throws Exception {
        final Related arguments = parseCommandLineArguments(new String[]{"--log", "x", "--verbose"}, Related::new);

        assertEquals("x", arguments.log);
        assertTrue("The verbose flag should be set", arguments.verbose);
    }

    @Test
    public void requiredOptionIsMissing() {
        try {
            parseCommandLineArguments(new String[]{"--log", "x"}, Related::new);
            fail("Parsing should have failed on the missing required option");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("The option '--log' requires '--verbose'", e.getMessage());
        }
    }

    @Test
    public void excludedOptionIsPresent() {
        try {
            parseCommandLineArguments(new String[]{"--quiet", "--verbose"}, Related::new);
            fail("Parsing should have failed on the excluded combination");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("The option '--quiet' cannot be used together with '--verbose'", e.getMessage());
        }
    }

    @Test
    public void excludedOptionIsAbsent() throws Exception {
        final Related arguments = parseCommandLineArguments(new String[]{"--quiet"}, Related::new);

        assertTrue("The quiet flag should be set", arguments.quiet);
    }

    @Test
    public void minOnANonNumericFieldIsAConfigurationError() {
        try {
            parseCommandLineArguments(new String[]{}, MinOnString::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidOptionConfiguration);
            assertEquals("The field 'file' declares min '1' and/or max '' which is not valid for a numeric field"
                    , e.getMessage());
        }
    }

    @Test
    public void aRegexThatDoesNotCompileIsAConfigurationError() {
        try {
            parseCommandLineArguments(new String[]{}, BadRegex::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidOptionConfiguration);
            assertEquals("The field 'file' declares a regex that does not compile", e.getMessage());
        }
    }

    @Test
    public void minAboveMaxIsAConfigurationError() {
        try {
            parseCommandLineArguments(new String[]{}, MinAboveMax::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidOptionConfiguration);
            assertEquals("The field 'port' declares min '10' and/or max '5' which is not valid for a numeric field"
                    , e.getMessage());
        }
    }

    @Test
    public void referringToAnUnknownOptionIsAConfigurationError() {
        try {
            parseCommandLineArguments(new String[]{}, UnknownRelated::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidOptionConfiguration);
            assertEquals("The field 'log' refers to 'nope' in its requires/excludes, but no option with that name exists"
                    , e.getMessage());
        }
    }

    @Test
    public void helpIsNotBlockedByABrokenRelation() throws Exception {
        final HelpWins arguments = parseCommandLineArguments(new String[]{"--log", "x", "--help"}, HelpWins::new);

        assertTrue("The help flag should be set", arguments.help);
        assertEquals("The broken requires must not stop --help", "x", arguments.log);
    }

}
