package unittests;

import jcli.annotations.CliOption;
import jcli.errors.*;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMissingArguments {

    public static class Arguments {
        @CliOption(isMandatory = true, longName = "file")
        public String file;
    }

    @Test
    public void parseUnknownCommandLineOption() {
        final String[] args = { "--something", "file.txt" };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Arguments::new));
    }

    @Test
    public void parseMissingArgument() {
        final String[] args = { };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Arguments::new));
    }

    @Test
    public void parseUnknownCommandLineValue() {
        final String[] args = { "--file" };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Arguments::new));
    }

}
