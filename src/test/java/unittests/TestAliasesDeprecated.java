package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestAliasesDeprecated {

    public static class Args {
        @CliOption(name = 'f', longName = "file", aliases = {"F", "filepath"})
        public String file;
        @CliOption(longName = "old", isDeprecated = true)
        public String old;
    }

    public static class DuplicateAlias {
        @CliOption(name = 'f', longName = "file", aliases = {"o"})
        public String file;
        @CliOption(name = 'o', longName = "other")
        public String other;
    }

    @Test
    public void shortAliasWorks() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{"-F", "data.txt"}, Args::new);

        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void longAliasWorks() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{"--filepath", "data.txt"}, Args::new);

        assertEquals("data.txt", arguments.file);
    }

    @Test
    public void aliasThatClashesIsAConfigurationError() {
        try {
            parseCommandLineArguments(new String[]{}, DuplicateAlias::new);
            fail("Parsing should have failed on the duplicate name");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidOptionConfiguration);
            assertEquals("The option name 'o' is used by more than one field: other", e.getMessage());
        }
    }

    @Test
    public void usingADeprecatedOptionWarns() throws Exception {
        final List<String> warnings = new ArrayList<>();

        final Args arguments = newCliParser(Args::new).deprecationConsumer(warnings::add)
                .parse(new String[]{"--old", "x"});

        assertEquals("x", arguments.old);
        assertEquals(List.of("The option '--old' is deprecated"), warnings);
    }

}
