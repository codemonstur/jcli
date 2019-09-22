import jcli.Argument;
import jcli.errors.InvalidArgumentConfiguration;
import jcli.errors.InvalidArgumentType;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.Set;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestInvalidArguments {

    public static class Arguments {
        @Argument(longName = "file")
        public final Set file;

        public Arguments(final Set file) {
            this.file = file;
        }
    }

    @Test(expected = InvalidArgumentType.class)
    public void argumentWithInvalidType() throws InvalidArgumentConfiguration, InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Arguments.class);

        fail("Parser failed to throw exception InvalidArgumentType");
    }

}
