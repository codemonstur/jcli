package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import org.junit.jupiter.api.Test;

import static jcli.CliHelp.getHelp;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPositionalHelp {

    public static class Arguments {
        @CliPositional
        public String file;
        @CliOption(name = 'h', isHelp = true, description = "Prints the help")
        public boolean help;
    }

    public static class LongPositional {
        @CliPositional(defaultValue = "some-really-long-positional-name")
        public String aVeryLongPositionalName;
        @CliOption(name = 'h', isHelp = true, description = "Prints the help")
        public boolean help;
    }

    @Test
    public void mandatoryPositionalIsDescribed() {
        final String help =
            "Usage: test [options] file\n" +
            "\n" +
            "Options:\n" +
            "   -h     optional  flag         Prints the help\n" +
            "   file   mandatory positional   \n";

        assertEquals(help, getHelp("test", Arguments.class), "The output help doesn't match");
    }

    @Test
    public void columnsAreAlignedWithThePositional() {
        final String help =
            "Usage: test [options] aVeryLongPositionalName\n" +
            "\n" +
            "Options:\n" +
            "   -h                        optional flag         Prints the help\n" +
            "   aVeryLongPositionalName   optional positional   [default: some-really-long-positional-name]\n";

        assertEquals(help, getHelp("test", LongPositional.class), "The output help doesn't match");
    }

}
