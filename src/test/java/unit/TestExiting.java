package unit;

import jcli.CliOption;
import jcli.CliParserBuilder;
import jcli.errors.InvalidCommandLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.OutputStream;
import java.io.PrintStream;

public class TestExiting {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    public static class HasHelp {
        @CliOption(isHelp = true, name = 'h')
        public boolean help;
    }

    public static class HasError {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
    }

    @Test
    public void exitOnHelp() throws InvalidCommandLine {
        exit.expectSystemExitWithStatus(1);

        PrintStream temp = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {}
        }));
        try {
            final String[] args = { "-h" };
            new CliParserBuilder<HasHelp>()
                    .name("test")
                    .object(HasHelp::new)
                    .onHelpPrintHelpAndExit()
                    .parse(args);
        } finally {
            System.setOut(temp);
        }
    }

    @Test
    public void exitOnError() throws InvalidCommandLine {
        exit.expectSystemExitWithStatus(2);

        PrintStream temp = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {}
        }));

        try {
            final String[] args = { };
            new CliParserBuilder<HasError>()
                    .name("test")
                    .object(HasError::new)
                    .onErrorPrintHelpAndExit()
                    .parse(args);
        } finally {
            System.setOut(temp);
        }
    }


}
