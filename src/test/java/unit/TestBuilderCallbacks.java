package unit;

import jcli.annotations.CliOption;
import jcli.CliParserBuilder;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertTrue;

public class TestBuilderCallbacks {

    private static class HasHelp {
        @CliOption(isHelp = true, name = 'h')
        private boolean help;
    }

    private static class HasError {
        @CliOption(longName = "file", isMandatory = true)
        private String file;
    }

    private static class Value<T> {
        private T value;
    }

    @Test
    public void callbackOnHelp() throws InvalidCommandLine {
        final Value<Boolean> wasCalled = new Value<>();
        wasCalled.value = FALSE;

        final String[] args = { "-h" };
        new CliParserBuilder<HasHelp>()
            .name("test")
            .object(HasHelp::new)
            .onHelpCall((arguments, args1) -> wasCalled.value = TRUE)
            .parse(args);

        assertTrue("Help callback wasn't called", wasCalled.value);
    }

    @Test
    public void callbackOnError() {
        final Value<Boolean> wasCalled = new Value<>();
        wasCalled.value = FALSE;

        try {
            final String[] args = { };
            new CliParserBuilder<HasError>()
                .name("test")
                .object(HasError::new)
                .onErrorCall((arguments, args1) -> wasCalled.value = TRUE)
                .parse(args);
        } catch (Exception e) {}

        assertTrue("Error callback wasn't called", wasCalled.value);
    }
}
