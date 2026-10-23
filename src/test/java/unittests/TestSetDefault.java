package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static jcli.CliHelp.getHelp;
import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        assertEquals("default", arguments.file, "The file argument is not equal");
    }

    @Test
    public void setDefaultOptional() throws InvalidCommandLine {
        final String[] args = { };
        final ArgumentsTwo arguments = parseCommandLineArguments(args, ArgumentsTwo::new);

        assertEquals(Optional.empty(), arguments.file, "The file argument is not equal");
    }

    @Test
    public void dontSetDefaultValue() throws InvalidCommandLine {
        final String[] args = { };
        final ArgumentsThree arguments = parseCommandLineArguments(args, ArgumentsThree::new);

        assertNull(arguments.file, "The file argument is not equal");
    }

    @Test
    public void includeDefaultValueInHelp() {
        final String help = """
                Usage: test [options]
                
                Options:
                   -f   optional value   [default: default]
                """;

        final String result = getHelp("test", ArgumentsThree.class);

        assertEquals(help, result, "The output help doesn't match");
    }

}
