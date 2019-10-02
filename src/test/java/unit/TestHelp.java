package unit;

import jcli.CliOption;
import org.junit.Test;

import static jcli.CliHelp.getHelp;
import static org.junit.Assert.assertEquals;

public class TestHelp {

    public static class Arguments {
        @CliOption(longName = "file", isMandatory = true, description = "The file to process")
        public String file;
    }

    @Test
    public void printHelp() {
        final String help =
            "usage: ./test [options]\n" +
            "\n" +
            "options:\n" +
            "\t(--file)\tmandatory value\t\tThe file to process\n";

        final String result = getHelp("test", Arguments.class);

        assertEquals("The output help doesn't match", help, result);
    }

}
