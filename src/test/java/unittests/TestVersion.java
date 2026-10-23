package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestVersion {

    private static class VersionAndHelp {
        @CliOption(name = 'v', longName = "version", isHelp = true, isVersion = true)
        private boolean version;
    }

    private static class VersionNotBoolean {
        @CliOption(name = 'v', longName = "version", isVersion = true)
        private String version;
    }

    @Test
    public void setVersionAndHelp() {
        final String[] args = { "--version" };

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, VersionAndHelp::new));
    }

    @Test
    public void setVersionNotBoolean() {
        final String[] args = { "--version" };

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, VersionNotBoolean::new));
    }

}
