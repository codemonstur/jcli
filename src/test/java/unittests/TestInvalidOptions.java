package unittests;

import jcli.annotations.CliOption;
import jcli.errors.*;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
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

    @Test(expected = InvalidOptionConfiguration.class)
    public void optionWithStaticModifier() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Static::new);

        fail("Parser failed to throw exception InvalidModifierStatic");
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void optionWithFinalModifier() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, Final::new);

        fail("Parser failed to throw exception InvalidModifierFinal");
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void optionWithoutName() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, NoName::new);

        fail("Parser failed to throw exception InvalidOptionName");
    }

}
