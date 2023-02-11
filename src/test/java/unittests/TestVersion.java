package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import static jcli.CliParser.parseCommandLineArguments;

public class TestVersion {

    private static class VersionAndHelp {
        @CliOption(name = 'v', longName = "version", isHelp = true, isVersion = true)
        private boolean version;
    }

    private static class VersionNotBoolean {
        @CliOption(name = 'v', longName = "version", isVersion = true)
        private String version;
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void setVersionAndHelp() throws InvalidCommandLine {
        final String[] args = { "--version" };
        final VersionAndHelp arguments = parseCommandLineArguments(args, VersionAndHelp::new);
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void setVersionNotBoolean() throws InvalidCommandLine {
        final String[] args = { "--version" };
        final VersionNotBoolean arguments = parseCommandLineArguments(args, VersionNotBoolean::new);
    }

}
