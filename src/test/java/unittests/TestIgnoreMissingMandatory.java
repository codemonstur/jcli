package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.*;

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
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("Missing argument '--file'", e.getMessage());
        }
    }

    @Test
    public void missingMandatoryIsIgnoredWhenConfigured() throws Exception {
        final Args arguments = newCliParser(Args::new).ignoreMissingMandatory().parse(new String[]{});

        assertNull(arguments.file, "The mandatory option stays null");
        assertNull(arguments.source, "The mandatory positional stays null");
        assertEquals(3, arguments.count, "Defaults are still applied");
    }

    public static class DefaultedArgs {
        @CliOption(longName = "file", isMandatory = true, defaultValue = "/tmp/default")
        public String file;
    }

    @Test
    public void theAnnotationDefaultSatisfiesAMandatoryOption() throws Exception {
        final DefaultedArgs arguments = newCliParser(DefaultedArgs::new).parse(new String[]{});

        assertEquals("/tmp/default", arguments.file, "A mandatory option is only missing when no source can provide a value");
    }

}
