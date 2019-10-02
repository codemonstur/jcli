import jcli.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestAttachedLongForm {

/*
    FIXME add this feature
    argv = { "-fdata.txt" }; // attached short form
*/

    public static class Arguments {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
    }

    @Test
    public void argumentWithAttachedLongForm() throws InvalidCommandLine {
        final String[] args = { "--file=data.txt" };
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);

        assertEquals("file field contains wrong data", "data.txt", arguments.file);
    }

}
