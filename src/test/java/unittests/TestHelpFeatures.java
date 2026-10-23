package unittests;

import jcli.CliHelp;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(help.startsWith("Does things"), "The description is the header:\n" + help);
        assertTrue(help.contains("Usage: tool [options] FILE"), help);
        assertTrue(help.contains("The file to read"), help);
        assertFalse(help.contains("secret"), "Hidden options are not shown:\n" + help);
        assertFalse(help.contains("Not in the help"), help);
        assertTrue(help.contains("[deprecated]"), "Deprecated options are marked:\n" + help);
        assertTrue(help.contains("FILE"), "Positionals show their name and description:\n" + help);
        assertTrue(help.contains("The file to process"), help);
        assertTrue(help.contains("Network:"), "Grouped options get their own section:\n" + help);
        assertTrue(help.endsWith("Made with jcli\n"), "The footer comes last:\n" + help);
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
        assertTrue(lines.length > unwrapped, "Wrapping should add lines: " + lines.length + " vs " + unwrapped);
        for (final String line : lines) {
            assertTrue(line.length() <= 60, "Line longer than 60 characters: '" + line + "'");
        }
    }

    @Test
    public void labelsComeFromTheResourceBundle() throws Exception {
        final StringBuilder captured = new StringBuilder();

        newCliParser(Args::new).name("tool").resourceBundle(new HelpBundle())
            .onHelpPrintHelpAndExit().exitHandler(code -> { }).helpConsumer(captured::append)
            .parse(new String[]{"-h"});

        final String help = captured.toString();
        assertTrue(help.contains("Verwendung: tool"), "The usage label should be translated:\n" + help);
        assertTrue(help.contains("Optionen:"), "The options label should be translated:\n" + help);
    }

}
