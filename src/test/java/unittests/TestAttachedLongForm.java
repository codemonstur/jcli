package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAttachedLongForm {

    public static class Arguments {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
    }

    @Test
    public void argumentWithAttachedLongForm() throws InvalidCommandLine {
        final String[] args = { "--file=data.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("data.txt", arguments.file, "file field contains wrong data");
    }

}
