package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.*;

public class TestExiting {

    public static class HelpArgs {
        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
        @CliOption(longName = "file")
        public String file;
    }

    public static class VersionArgs {
        @CliOption(name = 'v', longName = "version", isVersion = true)
        public boolean version;
    }

    @Test
    public void helpPathPrintsTheHelpAndExits() throws Exception {
        final int[] exit = { -1 };
        final StringBuilder help = new StringBuilder();

        final HelpArgs arguments = newCliParser(HelpArgs::new).name("tool")
            .onHelpPrintHelpAndExit().helpExitCode(7)
            .exitHandler(code -> exit[0] = code)
            .helpConsumer(help::append)
            .parse(new String[]{"-h"});

        assertEquals(7, exit[0], "The help exit code should be used");
        assertTrue(help.toString().contains("Usage: tool"), "The help should be printed:\n" + help);
        assertTrue(arguments.help, "The parsed arguments are still returned");
    }

    @Test
    public void errorPathPrintsHelpExitsAndRethrows() {
        final int[] exit = { -1 };
        final StringBuilder help = new StringBuilder();
        final StringBuilder error = new StringBuilder();

        try {
            newCliParser(HelpArgs::new).name("tool")
                .onErrorPrintHelpAndExit().errorExitCode(9)
                .exitHandler(code -> exit[0] = code)
                .helpConsumer(help::append)
                .errorConsumer(error::append)
                .parse(new String[]{"--nope"});
            fail("The exception should be rethrown when the exit handler returns");
        } catch (final InvalidCommandLine e) {
            assertEquals("'--nope' is not a valid command line argument", e.getMessage());
        }

        assertEquals(9, exit[0], "The error exit code should be used");
        assertEquals("[ERROR] '--nope' is not a valid command line argument\n", error.toString(), "The error line should have been printed");
        assertTrue(help.toString().contains("Usage: tool"), "The help should be printed:\n" + help);
    }

    @Test
    public void versionPathPrintsTheVersionAndExits() throws Exception {
        final int[] exit = { -1 };
        final String[] version = { null };

        newCliParser(VersionArgs::new).name("tool")
            .onVersionPrintVersionAndExit().versionExitCode(5)
            .exitHandler(code -> exit[0] = code)
            .versionConsumer((vendor, name, value) -> version[0] = name + "/" + value)
            .parse(new String[]{"--version"});

        assertEquals(5, exit[0], "The version exit code should be used");
        assertEquals("tool/" + versionOfTheJar(), version[0], "The version consumer should have been called with the parser name");
    }

    @Test
    public void versionCallbackIsCalledWithoutExiting() throws Exception {
        final String[] version = { null };

        final VersionArgs arguments = newCliParser(VersionArgs::new).name("tool")
            .onVersionCall((vendor, name, value) -> version[0] = name)
            .parse(new String[]{"--version"});

        assertEquals("tool", version[0]);
        assertTrue(arguments.version, "The parsed arguments are still returned");
    }

    /** The version comes from the manifest, which is absent when running from classes */
    private static String versionOfTheJar() {
        final Package currentPackage = jcli.CliParserBuilder.class.getPackage();
        return currentPackage.getImplementationVersion();
    }

}
