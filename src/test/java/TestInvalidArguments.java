import jcli.CliOption;
import jcli.errors.InvalidOptionConfiguration;
import jcli.errors.InvalidOptionType;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.Set;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestInvalidArguments {

    public static class Arguments {
        @CliOption(longName = "file")
        public final Set file;

        public Arguments() {
            file = null;
        }
    }

    @Test(expected = InvalidOptionType.class)
    public void argumentWithInvalidType() throws InvalidOptionConfiguration, InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw exception InvalidArgumentType");
    }

}
