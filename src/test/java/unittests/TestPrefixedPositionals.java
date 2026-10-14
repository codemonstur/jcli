package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestPrefixedPositionals {

    public static class Args {
        @CliOption(longName = "file")
        public String file;
        @CliPositional
        public String action;
    }

    @Test
    public void anUnknownOptionIsAnErrorByDefault() {
        try {
            newCliParser(Args::new).parse(new String[]{"--file", "f", "-x"});
            fail("Parsing should have failed on the unknown option");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("'-x' is not a valid command line argument", e.getMessage());
        }
    }

    @Test
    public void anUnknownOptionIsAPositionalWhenConfigured() throws Exception {
        final Args arguments = newCliParser(Args::new).allowPrefixedPositionals()
                .parse(new String[]{"--file", "f", "-x"});

        assertEquals("f", arguments.file);
        assertEquals("-x", arguments.action);
    }

    @Test
    public void knownOptionsStillParse() throws Exception {
        final Args arguments = newCliParser(Args::new).allowPrefixedPositionals()
                .parse(new String[]{"-x", "--file", "f"});

        assertEquals("f", arguments.file);
        assertEquals("-x", arguments.action);
    }

    @Test
    public void unknownLongFormIsAPositionalWhenConfigured() throws Exception {
        final Args arguments = newCliParser(Args::new).allowPrefixedPositionals()
                .parse(new String[]{"--nope"});

        assertEquals("--nope", arguments.action);
    }

}
