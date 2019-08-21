import jcli.Argument;
import jcli.errors.InvalidArgumentName;
import jcli.errors.InvalidArgumentType;
import jcli.errors.MissingArgument;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestMissingArguments {

    public static class Arguments {
        @Argument(mandatory = true, name = "file")
        public final String file;

        public Arguments(final String file) {
            this.file = file;
        }
    }

    @Test(expected = MissingArgument.class)
    public void parseMissingArgument() throws InvalidArgumentName, MissingArgument, InvalidArgumentType {
        final String[] args = { "-something", "file.txt" };
        parseCommandLineArguments(args, Arguments.class);

        fail("Parser failed to throw exception");
    }

}
