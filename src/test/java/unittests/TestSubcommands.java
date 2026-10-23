package unittests;

import jcli.CommandResult;
import jcli.CliParserBuilder;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.*;

public class TestSubcommands {

    public static class Root {
        @CliOption(name = 'v', longName = "verbose")
        public boolean verbose;
        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
    }

    public static class Build {
        @CliOption(longName = "output")
        public String output;
        @CliPositional
        public String target;
    }

    public static class Repeat {
        @CliOption(longName = "repeat")
        public int repeat;
    }

    public static class RemoteRoot {
        @CliOption(name = 'f', longName = "force")
        public boolean force;
    }

    public static class Add {
        @CliOption(longName = "url")
        public String url;
    }

    public static class RootWithPositional {
        @CliPositional
        public String source;
    }

    private CliParserBuilder<Root> tool() {
        return newCliParser(Root::new).name("tool")
            .subcommand("build", "Build the thing", newCliParser(Build::new))
            .subcommand("test", "Run the tests", newCliParser(Repeat::new));
    }

    @Test
    public void subcommandIsSelectedByTheFirstBareToken() throws Exception {
        final CommandResult<Root> result = tool()
                .parseWithSubcommands(new String[]{"--verbose", "build", "--output", "out", "main"});

        assertTrue(result.getRoot().verbose, "The root verbose flag should be set");
        assertEquals("build", result.getSubcommand());
        assertEquals(List.of("build"), result.getPath());

        final Build build = result.get(Build.class);
        assertEquals("out", build.output);
        assertEquals("main", build.target);
    }

    @Test
    public void commandLineWithoutASubcommand() throws Exception {
        final CommandResult<Root> result = tool().parseWithSubcommands(new String[]{"--verbose"});

        assertTrue(result.getRoot().verbose, "The root verbose flag should be set");
        assertFalse(result.hasSubcommand());
        assertNull(result.getSubcommand());
        assertEquals(List.of(), result.getPath());
    }

    @Test
    public void unknownSubcommandSuggestsTheClosestOne() {
        try {
            tool().parseWithSubcommands(new String[]{"buld"});
            fail("Parsing should have failed on the unknown subcommand");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("'buld' is not a valid command. Did you mean 'build'?", e.getMessage());
        }
    }

    @Test
    public void nestedSubcommands() throws Exception {
        final CommandResult<Root> result = newCliParser(Root::new).name("tool")
            .subcommand("remote", newCliParser(RemoteRoot::new)
                .subcommand("add", newCliParser(Add::new)))
            .parseWithSubcommands(new String[]{"remote", "--force", "add", "--url", "http://example.com"});

        assertEquals(List.of("remote", "add"), result.getPath());
        assertEquals("add", result.getSubcommand());
        assertTrue(result.get(RemoteRoot.class).force, "The remote force flag should be set");
        assertEquals("http://example.com", result.get(Add.class).url);
    }

    @Test
    public void parseWithSubcommandsRegisteredIsAConfigurationError() {
        try {
            tool().parse(new String[]{"build"});
            fail("Parsing should have failed on the wrong entry point");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("This parser has subcommands registered, use parseWithSubcommands instead of parse"
                    , e.getMessage());
        }
    }

    @Test
    public void rootWithPositionalsIsAConfigurationError() {
        try {
            newCliParser(RootWithPositional::new).name("tool")
                .subcommand("build", newCliParser(Build::new))
                .parseWithSubcommands(new String[]{"build", "main"});
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("The subcommand 'tool' is registered on a class that also has positional arguments, this is not supported"
                    , e.getMessage());
        }
    }

    @Test
    public void duplicateSubcommandIsAConfigurationError() {
        try {
            newCliParser(Root::new).name("tool")
                .subcommand("build", newCliParser(Build::new))
                .subcommand("build", newCliParser(Repeat::new));
            fail("Registering should have failed on the duplicate subcommand");
        } catch (final InvalidOptionConfiguration e) {
            assertEquals("The subcommand 'build' is registered more than once", e.getMessage());
        }
    }

    @Test
    public void helpListsTheSubcommands() throws Exception {
        final StringBuilder help = new StringBuilder();

        tool().onHelpPrintHelpAndExit().exitHandler(code -> { }).helpConsumer(help::append)
            .parseWithSubcommands(new String[]{"-h"});

        assertTrue(help.toString().contains("Commands:"), "The help should list the commands:\n" + help);
        assertTrue(help.toString().contains("build"), "The help should list build:\n" + help);
        assertTrue(help.toString().contains("Build the thing"), "The help should show the subcommand description:\n" + help);
    }

}
