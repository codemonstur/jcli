import jcli.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionType;
import org.junit.Test;

import java.util.Set;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestInvalidArguments {

    public static class Arguments {
        @CliOption(longName = "file")
        public Set file;
    }

    @Test(expected = InvalidOptionType.class)
    public void argumentWithInvalidType() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw exception InvalidArgumentType");
    }

}
