package unit;

import jcli.annotations.CliOption;
import jcli.errors.*;
import org.junit.Test;

import java.net.URI;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.fail;

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

    @Test(expected = CommandLineArgumentTooShort.class)
    public void argumentSingleDash() throws InvalidCommandLine {
        final String[] args = { "-" };
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw exception CommandLineArgumentTooShort");
    }

    @Test(expected = CommandLineArgumentTooShort.class)
    public void argumentDoubleDash() throws InvalidCommandLine {
        final String[] args = { "--" };
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw exception CommandLineArgumentTooShort");
    }

    @Test(expected = SingleDashAttachedFormArgument.class)
    public void argumentLongSingleDash() throws InvalidCommandLine {
        final String[] args = { "-file", "data.txt" };
        parseCommandLineArguments(args, Arguments::new);

        fail("Parser failed to throw exception SingleDashLongFormArgument");
    }

    @Test(expected = AttachedFormFlagArgument.class)
    public void argumentLongFlag() throws InvalidCommandLine {
        final String[] args = { "-f=boe" };
        parseCommandLineArguments(args, Flag::new);

        fail("Parser failed to throw exception LongFormFlagArgument");
    }

    @Test(expected = InvalidArgumentValue.class)
    public void argumentWrongValue() throws InvalidCommandLine {
        final String[] args = { "-o", "%&^*%" };
        parseCommandLineArguments(args, WrongValue::new);

        fail("Parser failed to throw exception InvalidArgumentValue");
    }
}
