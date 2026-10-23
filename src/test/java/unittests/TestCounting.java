package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestCounting {

    public static class Args {
        @CliOption(name = 'v', longName = "verbose", isCounting = true)
        public int verbose;
        @CliOption(name = 'q', longName = "quiet", isCounting = true)
        public Long quiet;
    }

    public static class StartAtFive {
        @CliOption(name = 'v', longName = "verbose", isCounting = true, defaultValue = "5")
        public int verbose;
    }

    public static class CountingOnString {
        @CliOption(name = 'v', longName = "verbose", isCounting = true)
        public String verbose;
    }

    public static class CountingMandatory {
        @CliOption(name = 'v', longName = "verbose", isCounting = true, isMandatory = true)
        public int verbose;
    }

    public static class CountingWithArity {
        @CliOption(name = 'v', longName = "verbose", isCounting = true, arity = 1)
        public int verbose;
    }

    @Test
    public void countsGroupedFlags() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{"-vvv"}, Args::new);

        assertEquals(3, arguments.verbose);
    }

    @Test
    public void countsAcrossTokens() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{"-v", "--verbose", "-v"}, Args::new);

        assertEquals(3, arguments.verbose);
    }

    @Test
    public void absentIsZero() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{}, Args::new);

        assertEquals(0, arguments.verbose);
        assertEquals(Long.valueOf(0L), arguments.quiet);
    }

    @Test
    public void countsLongs() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{"-qq"}, Args::new);

        assertEquals(Long.valueOf(2L), arguments.quiet);
    }

    @Test
    public void startsAtTheDefaultValue() throws Exception {
        final StartAtFive arguments = parseCommandLineArguments(new String[]{"-v"}, StartAtFive::new);

        assertEquals(6, arguments.verbose);
    }

    @Test
    public void countingNeedsANumericField() {
        try {
            parseCommandLineArguments(new String[]{}, CountingOnString::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("The field 'verbose' is a counting flag, this requires an int, Integer, long or Long field"
                    , e.getMessage());
        }
    }

    @Test
    public void countingCannotBeMandatory() {
        try {
            parseCommandLineArguments(new String[]{}, CountingMandatory::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("The field 'verbose' is a counting flag and cannot be mandatory, a count always has a value"
                    , e.getMessage());
        }
    }

    @Test
    public void countingCannotDeclareAnArity() {
        try {
            parseCommandLineArguments(new String[]{}, CountingWithArity::new);
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("The field 'verbose' is a counting flag and cannot declare an arity", e.getMessage());
        }
    }

}
