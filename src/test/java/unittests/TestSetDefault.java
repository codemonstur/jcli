package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.Optional;

import static jcli.CliHelp.getHelp;
import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestSetDefault {

    public static class ArgumentsOne {
        @CliOption(name = 'f', defaultValue = "default")
        public String file;
    }

    public static class ArgumentsTwo {
        @CliOption(name = 'f', setDefaultWhenMissing = false, defaultValue = "default")
        public Optional<String> file;
    }

    public static class ArgumentsThree {
        @CliOption(name = 'f', setDefaultWhenMissing = false, defaultValue = "default")
        public String file;
    }

    @Test
    public void setDefaultValue() throws InvalidCommandLine {
        final String[] args = { };
        final ArgumentsOne arguments = parseCommandLineArguments(args, ArgumentsOne::new);

        assertEquals("The file argument is not equal", "default", arguments.file);
    }

    @Test
    public void setDefaultOptional() throws InvalidCommandLine {
        final String[] args = { };
        final ArgumentsTwo arguments = parseCommandLineArguments(args, ArgumentsTwo::new);

        assertEquals("The file argument is not equal", Optional.empty(), arguments.file);
    }

    @Test
    public void dontSetDefaultValue() throws InvalidCommandLine {
        final String[] args = { };
        final ArgumentsThree arguments = parseCommandLineArguments(args, ArgumentsThree::new);

        assertEquals("The file argument is not equal", null, arguments.file);
    }

    @Test
    public void includeDefaultValueInHelp() {
        final String help = "Usage: test [options] \n" +
            "\n" +
            "Options:\n" +
            "   -f   optional value   [default: default]\n";

        final String result = getHelp("test", ArgumentsThree.class);

        assertEquals("The output help doesn't match", help, result);

    }

}
