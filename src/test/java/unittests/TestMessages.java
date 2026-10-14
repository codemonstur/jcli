package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.ListResourceBundle;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestMessages {

    public static class Args {
        @CliOption(longName = "count")
        public int count;
        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
    }

    public static class GermanBundle extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][]{
                {"help.usage", "Verwendung:"},
                {"error.missingValue", "Argument ''{0}'' hat keinen Wert"},
                {"error.unknownArgument", "''{0}'' ist kein gültiges Argument"},
                {"error.didYouMean", "Meintest du ''{0}''?"},
            };
        }
    }

    @Test
    public void errorMessagesAreTranslated() {
        try {
            newCliParser(Args::new).resourceBundle(new GermanBundle()).parse(new String[]{"--count"});
            fail("Parsing should have failed on the missing value");
        } catch (final InvalidCommandLine e) {
            assertEquals("Argument '--count' hat keinen Wert", e.getMessage());
        }
    }

    @Test
    public void suggestionsAreTranslated() {
        try {
            newCliParser(Args::new).resourceBundle(new GermanBundle())
                .parse(new String[]{"--coun", "5"});
            fail("Parsing should have failed on the typo");
        } catch (final InvalidCommandLine e) {
            assertEquals("'--coun' ist kein gültiges Argument. Meintest du '--count'?", e.getMessage());
        }
    }

    @Test
    public void untranslatedBundlesKeepEnglish() {
        try {
            newCliParser(Args::new).resourceBundle(new GermanBundle())
                .parse(new String[]{"--count", "5", "--coun"});
            fail("Parsing should have failed on the typo");
        } catch (final InvalidCommandLine e) {
            // the unknownArgument key IS translated, so this one is German
            assertEquals("'--coun' ist kein gültiges Argument. Meintest du '--count'?", e.getMessage());
        }
    }

    @Test
    public void getHelpTextAppliesTheBuilderSettings() {
        final String help = newCliParser(Args::new).name("zaehler")
                .resourceBundle(new GermanBundle()).getHelpText();

        assertTrue("The bundle translates the labels:\n" + help, help.contains("Verwendung: zaehler"));
        assertTrue("The options are documented:\n" + help, help.contains("--count"));
    }

}
