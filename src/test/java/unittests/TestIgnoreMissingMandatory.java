package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestIgnoreMissingMandatory {

    public static class Args {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
        @CliOption(longName = "count", isMandatory = true, defaultValue = "3")
        public int count;
        @CliPositional
        public String source;
    }

    @Test
    public void missingMandatoryOptionFailsByDefault() {
        try {
            newCliParser(Args::new).parse(new String[]{});
            fail("Parsing should have failed on the missing mandatory option");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("Missing argument '--file'", e.getMessage());
        }
    }

    @Test
    public void missingMandatoryIsIgnoredWhenConfigured() throws Exception {
        final Args arguments = newCliParser(Args::new).ignoreMissingMandatory().parse(new String[]{});

        assertNull("The mandatory option stays null", arguments.file);
        assertNull("The mandatory positional stays null", arguments.source);
        assertEquals("Defaults are still applied", 3, arguments.count);
    }

    public static class DefaultedArgs {
        @CliOption(longName = "file", isMandatory = true, defaultValue = "/tmp/default")
        public String file;
    }

    @Test
    public void theAnnotationDefaultSatisfiesAMandatoryOption() throws Exception {
        final DefaultedArgs arguments = newCliParser(DefaultedArgs::new).parse(new String[]{});

        assertEquals("A mandatory option is only missing when no source can provide a value"
                , "/tmp/default", arguments.file);
    }

}
