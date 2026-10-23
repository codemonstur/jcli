package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import static java.lang.Boolean.*;
import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.*;

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

        assertNull(arguments.help, "The help field should have been left alone");
        assertNull(arguments.verbose, "The verbose field should have been left alone");
    }

    @Test
    public void wrapperFlagIsSetWhenGiven() throws InvalidCommandLine {
        final WrapperFlags arguments = newCliParser(WrapperFlags::new).name("test")
            .parse(new String[]{"-h"});

        assertEquals(TRUE, arguments.help, "The help field should be set");
    }

    @Test
    public void primitiveFlagStillWorks() throws InvalidCommandLine {
        final WrapperFlags not = newCliParser(WrapperFlags::new).name("test").parse(new String[]{});
        assertNull(not.verbose, "A wrapper boolean should not be false by default");

        final PrimitiveFlags set = newCliParser(PrimitiveFlags::new).name("test").parse(new String[]{"-h"});
        final PrimitiveFlags unset = newCliParser(PrimitiveFlags::new).name("test").parse(new String[]{});

        assertTrue(set.help, "The help flag should be set");
        assertFalse(unset.help, "The help flag should not be set");
    }

    @Test
    public void parseSuppressErrorsReturnsTheInstance() {
        final WrapperFlags arguments = newCliParser(WrapperFlags::new).name("test")
            .parseSuppressErrors(new String[]{});

        assertNotNull(arguments, "Parsing should not have failed silently");
    }

}
