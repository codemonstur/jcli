package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.annotations.CliUnderscoreIsDash;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnum {

    private static class EnumTypes {
        @CliOption(isMandatory = true, longName = "test")
        private ArgumentEnum enumTest;

        @CliOption(isMandatory = true, longName = "underscore")
        private ArgumentEnum enumDash;

        @CliPositional(defaultValue = "dummy")
        private ArgumentEnum action;

        @CliPositional(defaultValue = "dummy")
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

        assertEquals(ArgumentEnum.dash_underscore, arguments.enumDash, "Dash not converted to underscore");
        assertEquals(ArgumentEnum.test, arguments.enumTest, "Correct value not converted");
        assertEquals(ArgumentEnum.something, arguments.action, "Action positional has wrong value");
        assertEquals(ArgumentEnum.dummy, arguments.subaction, "Subaction positional is not the default");
    }

}
