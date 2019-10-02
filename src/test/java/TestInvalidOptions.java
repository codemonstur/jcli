import jcli.CliOption;
import jcli.errors.*;
import org.junit.Test;

import static jcli.Parser.parseCommandLineArguments;
import static org.junit.Assert.fail;

public class TestInvalidOptions {

    public static class Static {
        @CliOption(longName = "file", isMandatory = true)
        public static String file;
    }
    public static class Final {
        @CliOption(longName = "file", isMandatory = true)
        public final String file;

        public Final() {
            file = null;
        }
    }
    public static class NoName {
        @CliOption(isMandatory = true)
        public String file;
    }
    public static class NotBoolean {
        @CliOption(longName = "file", isFlag = true, isMandatory = true)
        public String file;
    }
    public static class MissingDefault {
        @CliOption(longName = "file")
        public String file;
    }

    @Test(expected = InvalidModifierStatic.class)
    public void optionWithStaticModifier() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Static::new);

        fail("Parser failed to throw exception InvalidModifierStatic");
    }

    @Test(expected = InvalidModifierFinal.class)
    public void optionWithFinalModifier() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Final::new);

        fail("Parser failed to throw exception InvalidModifierFinal");
    }

    @Test(expected = InvalidOptionName.class)
    public void optionWithoutName() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, NoName::new);

        fail("Parser failed to throw exception InvalidOptionName");
    }

    @Test(expected = FlagTypeNotBoolean.class)
    public void optionFlagNotBoolean() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, NotBoolean::new);

        fail("Parser failed to throw exception FlagTypeNotBoolean");
    }

    @Test(expected = MissingDefaultForOption.class)
    public void optionMissingDefault() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, MissingDefault::new);

        fail("Parser failed to throw exception MissingDefaultForOption");
    }
}
