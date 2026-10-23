package unittests;

import jcli.annotations.CliOption;
import jcli.errors.*;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestInvalidArguments {

    public static class Arguments {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
    }
    public static class Flag {
        @CliOption(name = 'f')
        public boolean file;
    }
    public static class WrongValue {
        @CliOption(name = 'o', isMandatory = true)
        public URI uri;
    }

    @Test
    public void argumentSingleDash() {
        final String[] args = { "-" };
        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Arguments::new));
    }

    @Test
    public void argumentDoubleDash() {
        final String[] args = { "--" };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Arguments::new));
    }

    @Test
    public void argumentLongSingleDash() {
        final String[] args = { "-file", "data.txt" };
        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Arguments::new));
    }

    @Test
    public void argumentLongFlag() {
        final String[] args = { "-f=boe" };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, Flag::new));
    }

    @Test
    public void argumentWrongValue() {
        final String[] args = { "-o", "%&^*%" };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, WrongValue::new));
    }

}
