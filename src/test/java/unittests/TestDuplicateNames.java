package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void duplicatedLongNameIsRejected() {
        final String[] args = { "--file", "file.txt" };

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, DuplicatedLongNames::new));
    }

    @Test
    public void shortAndLongCollisionIsRejected() {
        final String[] args = { "-f", "file.txt" };

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, ShortAndLongCollision::new));
    }

    @Test
    public void oneFieldCanDeclareBothNames() throws InvalidCommandLine {
        final String[] shortArgs = { "-f", "short.txt" };
        final SameFieldTwoNames fromShort = parseCommandLineArguments(shortArgs, SameFieldTwoNames::new);
        assertEquals("short.txt", fromShort.file, "The short name did not work");

        final String[] longArgs = { "--f", "long.txt" };
        final SameFieldTwoNames fromLong = parseCommandLineArguments(longArgs, SameFieldTwoNames::new);
        assertEquals("long.txt", fromLong.file, "The long name did not work");
    }

}
