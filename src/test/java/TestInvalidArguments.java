import jcli.Argument;
import jcli.errors.InvalidArgumentName;
import jcli.errors.InvalidArgumentType;
import jcli.errors.MissingArgument;
import org.junit.Test;

import java.util.Set;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestInvalidArguments {

    public static class Arguments {
        @Argument(name = "file")
        public final Set file;

        public Arguments(final Set file) {
            this.file = file;
        }
    }

    @Test(expected = InvalidArgumentType.class)
    public void parseMissingArgument() throws InvalidArgumentName, MissingArgument, InvalidArgumentType {
        final String[] args = { "-file", "file.txt" };
        parseCommandLineArguments(args, Arguments.class);

        fail("Parser failed to throw exception");
    }

}
