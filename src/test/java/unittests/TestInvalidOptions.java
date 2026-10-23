package unittests;

import jcli.annotations.CliOption;
import jcli.errors.*;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestInvalidOptions {

    public static class Static {
        @CliOption(longName = "file", isMandatory = true)
        public static String file;
    }
    public static class NoName {
        @CliOption(isMandatory = true)
        public String file;
    }

    @Test
    public void optionWithStaticModifier() {
        final String[] args = {};

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, Static::new));
    }

    @Test
    public void optionWithoutName() {
        final String[] args = {};

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, NoName::new));
    }

}
