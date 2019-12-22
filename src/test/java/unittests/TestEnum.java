package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.annotations.CliUnderscoreIsDash;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestEnum {

    private static class EnumTypes {
        @CliOption(isMandatory = true, longName = "test")
        private ArgumentEnum enumTest;

        @CliOption(isMandatory = true, longName = "underscore")
        private ArgumentEnum enumDash;

        @CliPositional(index = 0, defaultValue = "dummy")
        private ArgumentEnum action;

        @CliPositional(index = 1, defaultValue = "dummy")
        private ArgumentEnum subaction;
    }

    @CliUnderscoreIsDash
    private enum ArgumentEnum {
        test, something, dummy, dash_underscore
    }

    @Test
    public void enumArguments() throws InvalidCommandLine {
        final String[] args = { "--underscore", "dash-underscore", "--test", "test", "something" };
        final EnumTypes arguments = parseCommandLineArguments(args, EnumTypes::new);

        assertEquals("Dash not converted to underscore", ArgumentEnum.dash_underscore, arguments.enumDash);
        assertEquals("Correct value not converted", ArgumentEnum.test, arguments.enumTest);
        assertEquals("Action positional has wrong value", ArgumentEnum.something, arguments.action);
        assertEquals("Subaction positional is not the default", ArgumentEnum.dummy, arguments.subaction);
    }

}
