package unittests;

import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import org.junit.Test;

import java.net.URI;
import java.util.OptionalLong;

import static jcli.CliHelp.getHelp;
import static org.junit.Assert.assertEquals;

public class TestHelp {

    public static class Arguments {
        @CliOption(longName = "file", isMandatory = true, description = "The file to process")
        public String file;
    }

    @CliCommand(name = "help",
                description = "A dummy command that is only good for showing help",
                examples = {"-f somefile.txt delete", "-h"})
    public static class LotsOfHelp {
        @CliOption(name = 'f', longName = "file", isMandatory = true, description = "The file to process")
        public String file;
        @CliOption(isHelp = true, name = 'h', description = "Prints the help")
        private boolean help;
        @CliOption(name = 'o', isMandatory = true, description = "A URI, maybe youtube or something")
        public URI uri;
        @CliOption(name = 'l', defaultValue = "")
        public OptionalLong optionalLong;
        @CliPositional
        public String action;
    }

    @Test
    public void printHelp() {
        final String help =
            "Usage: test [options] \n" +
            "\n" +
            "Options:\n" +
            "   --file   mandatory value   The file to process\n";

        final String result = getHelp("test", Arguments.class);

        assertEquals("The output help doesn't match", help, result);
    }

    @Test
    public void printLotsOfHelp() {
        final String help =
            "A dummy command that is only good for showing help\n" +
            "\n" +
            "Examples:\n" +
            "   help -f somefile.txt delete\n" +
            "   help -h\n" +
            "\n" +
            "Usage: help [options] action\n" +
            "\n" +
            "Options:\n" +
            "   -f --file   mandatory value   The file to process\n" +
            "   -h          optional  flag    Prints the help\n" +
            "   -o          mandatory value   A URI, maybe youtube or something\n" +
            "   -l          optional  value   \n";

        final String result = getHelp(null, LotsOfHelp.class);

        assertEquals("The output help doesn't match", help, result);
    }
}
