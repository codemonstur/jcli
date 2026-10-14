package unittests;

import jcli.CliParserBuilder;
import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestInteractive {

    public static class Password {
        @CliOption(longName = "password", isInteractive = true)
        public String password;
    }

    public static class InteractiveFlag {
        @CliOption(longName = "force", isInteractive = true)
        public boolean force;
    }

    @Test
    public void promptsWhenTheValueIsMissing() throws Exception {
        final Password arguments = newCliParser(Password::new)
                .prompter(message -> {
                    assertEquals("Enter a value for --password: ", message);
                    return "secret";
                })
                .parse(new String[]{"--password"});

        assertEquals("secret", arguments.password);
    }

    @Test
    public void commandLineValueBeatsThePrompt() throws Exception {
        final Password arguments = newCliParser(Password::new)
                .prompter(message -> {
                    fail("The prompt should not be called when there is a value");
                    return null;
                })
                .parse(new String[]{"--password", "hunter2"});

        assertEquals("hunter2", arguments.password);
    }

    @Test
    public void cancelledPromptIsAnError() {
        final CliParserBuilder<Password> builder = newCliParser(Password::new).prompter(message -> null);
        try {
            builder.parse(new String[]{"--password"});
            fail("Parsing should have failed on the cancelled prompt");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertEquals("Could not read a value for '--password'", e.getMessage());
        }
    }

    @Test
    public void interactiveFlagIsAConfigurationError() {
        try {
            newCliParser(InteractiveFlag::new).parse(new String[]{});
            fail("Parsing should have failed on the invalid configuration");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidOptionConfiguration);
            assertEquals("The field 'force' is interactive but is a flag. Interactive options take a value, so set an arity of 1."
                    , e.getMessage());
        }
    }

}
