package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestDuplicateNames {

    public static class DuplicatedLongNames {
        @CliOption(longName = "file", isMandatory = true)
        public String first;
        @CliOption(longName = "file")
        public String second;
    }

    public static class ShortAndLongCollision {
        @CliOption(name = 'f')
        public String first;
        @CliOption(longName = "f")
        public String second;
    }

    public static class SameFieldTwoNames {
        @CliOption(name = 'f', longName = "f")
        public String file;
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void duplicatedLongNameIsRejected() throws InvalidCommandLine {
        final String[] args = { "--file", "file.txt" };
        parseCommandLineArguments(args, DuplicatedLongNames::new);
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void shortAndLongCollisionIsRejected() throws InvalidCommandLine {
        final String[] args = { "-f", "file.txt" };
        parseCommandLineArguments(args, ShortAndLongCollision::new);
    }

    @Test
    public void oneFieldCanDeclareBothNames() throws InvalidCommandLine {
        final String[] shortArgs = { "-f", "short.txt" };
        final SameFieldTwoNames fromShort = parseCommandLineArguments(shortArgs, SameFieldTwoNames::new);
        assertEquals("The short name did not work", "short.txt", fromShort.file);

        final String[] longArgs = { "--f", "long.txt" };
        final SameFieldTwoNames fromLong = parseCommandLineArguments(longArgs, SameFieldTwoNames::new);
        assertEquals("The long name did not work", "long.txt", fromLong.file);
    }

}
