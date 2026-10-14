package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestBooleanFlags {

    public static class WrapperFlags {
        @CliOption(isHelp = true, name = 'h')
        public Boolean help;
        @CliOption(longName = "verbose")
        public Boolean verbose;
    }

    public static class PrimitiveFlags {
        @CliOption(isHelp = true, name = 'h')
        public boolean help;
    }

    @Test
    public void wrapperFlagWithoutValueDoesNotThrow() throws InvalidCommandLine {
        final WrapperFlags arguments = newCliParser(WrapperFlags::new).name("test").parse(new String[]{});

        assertNull("The help field should have been left alone", arguments.help);
        assertNull("The verbose field should have been left alone", arguments.verbose);
    }

    @Test
    public void wrapperFlagIsSetWhenGiven() throws InvalidCommandLine {
        final WrapperFlags arguments = newCliParser(WrapperFlags::new).name("test")
            .parse(new String[]{"-h"});

        assertEquals("The help field should be set", Boolean.TRUE, arguments.help);
    }

    @Test
    public void primitiveFlagStillWorks() throws InvalidCommandLine {
        final WrapperFlags not = newCliParser(WrapperFlags::new).name("test").parse(new String[]{});
        assertNull("A wrapper boolean should not be false by default", not.verbose);

        final PrimitiveFlags set = newCliParser(PrimitiveFlags::new).name("test").parse(new String[]{"-h"});
        final PrimitiveFlags unset = newCliParser(PrimitiveFlags::new).name("test").parse(new String[]{});

        assertTrue("The help flag should be set", set.help);
        assertFalse("The help flag should not be set", unset.help);
    }

    @Test
    public void parseSuppressErrorsReturnsTheInstance() {
        final WrapperFlags arguments = newCliParser(WrapperFlags::new).name("test")
            .parseSuppressErrors(new String[]{});

        assertNotNull("Parsing should not have failed silently", arguments);
    }

}
