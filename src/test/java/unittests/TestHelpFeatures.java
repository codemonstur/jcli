package unittests;

import jcli.CliHelp;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import org.junit.Test;

import java.util.ListResourceBundle;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestHelpFeatures {

    @CliCommand(name = "tool", description = "Does things", footer = "Made with jcli")
    public static class Args {
        @CliOption(longName = "file", description = "The file to read")
        public String file;
        @CliOption(longName = "secret", description = "Not in the help", isHidden = true)
        public String secret;
        @CliOption(longName = "old", description = "The old way", isDeprecated = true)
        public String old;
        @CliOption(name = 'n', longName = "host", description = "The host to connect to", group = "Network")
        public String host;
        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
        @CliPositional(name = "FILE", description = "The file to process")
        public String toProcess;
    }

    public static class HelpBundle extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][]{
                {"help.usage", "Verwendung:"},
                {"help.options", "Optionen:"},
            };
        }
    }

    @Test
    public void helpShowsDescriptionsGroupsFooterAndHidesTheHidden() {
        final String help = CliHelp.getHelp(Args.class);

        assertTrue("The description is the header:\n" + help, help.startsWith("Does things"));
        assertTrue(help, help.contains("Usage: tool [options] FILE"));
        assertTrue(help, help.contains("The file to read"));
        assertFalse("Hidden options are not shown:\n" + help, help.contains("secret"));
        assertFalse(help, help.contains("Not in the help"));
        assertTrue("Deprecated options are marked:\n" + help, help.contains("[deprecated]"));
        assertTrue("Positionals show their name and description:\n" + help, help.contains("FILE"));
        assertTrue(help, help.contains("The file to process"));
        assertTrue("Grouped options get their own section:\n" + help, help.contains("Network:"));
        assertTrue("The footer comes last:\n" + help, help.endsWith("Made with jcli\n"));
    }

    @Test
    public void wrappingKeepsLinesWithinTheConfiguredWidth() throws Exception {
        final StringBuilder captured = new StringBuilder();

        newCliParser(Args::new).name("tool").helpWidth(60)
            .onHelpPrintHelpAndExit().exitHandler(code -> { }).helpConsumer(captured::append)
            .parse(new String[]{"-h"});

        final String help = captured.toString();
        final int unwrapped = CliHelp.getHelp(Args.class).split("\n").length;
        final String[] lines = help.split("\n");
        assertTrue("Wrapping should add lines: " + lines.length + " vs " + unwrapped, lines.length > unwrapped);
        for (final String line : lines) {
            assertTrue("Line longer than 60 characters: '" + line + "'", line.length() <= 60);
        }
    }

    @Test
    public void labelsComeFromTheResourceBundle() throws Exception {
        final StringBuilder captured = new StringBuilder();

        newCliParser(Args::new).name("tool").resourceBundle(new HelpBundle())
            .onHelpPrintHelpAndExit().exitHandler(code -> { }).helpConsumer(captured::append)
            .parse(new String[]{"-h"});

        final String help = captured.toString();
        assertTrue("The usage label should be translated:\n" + help, help.contains("Verwendung: tool"));
        assertTrue("The options label should be translated:\n" + help, help.contains("Optionen:"));
    }

}
