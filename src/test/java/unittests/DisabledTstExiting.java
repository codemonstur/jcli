package unittests;

import jcli.CliParserBuilder;
import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Rule;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;

public class DisabledTstExiting {
    // FIXME these tests should work but there are multiple issues:
    //  - Bob has trouble finding the necessary libraries because they are not hosted on MavenCentral
    //  - The tests die because the code that intercepts System.exit() uses a Security manager which is deprecated
    // Used dependency is:
    // #  - repository: com.github.stefanbirkner:system-rules:1.19.0
    // #    scope: test
    // Disabled for now

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
//
//    public static class HasHelp {
//        @CliOption(isHelp = true, name = 'h')
//        public boolean help;
//    }
//
//    public static class HasError {
//        @CliOption(longName = "file", isMandatory = true)
//        public String file;
//    }
//
//    @Test
//    public void exitOnHelp() throws InvalidCommandLine {
//        exit.expectSystemExitWithStatus(1);
//
//        PrintStream temp = System.out;
//        System.setOut(new PrintStream(new OutputStream() {
//            public void write(int b) {}
//        }));
//        try {
//            final String[] args = { "-h" };
//            new CliParserBuilder<HasHelp>()
//                    .name("test")
//                    .object(HasHelp::new)
//                    .onHelpPrintHelpAndExit()
//                    .parse(args);
//        } finally {
//            System.setOut(temp);
//        }
//    }
//
//    @Test
//    public void exitOnError() throws InvalidCommandLine {
//        exit.expectSystemExitWithStatus(2);
//
//        PrintStream temp = System.out;
//        System.setOut(new PrintStream(new OutputStream() {
//            public void write(int b) {}
//        }));
//
//        try {
//            final String[] args = { };
//            new CliParserBuilder<HasError>()
//                    .name("test")
//                    .object(HasError::new)
//                    .onErrorPrintHelpAndExit()
//                    .parse(args);
//        } finally {
//            System.setOut(temp);
//        }
//    }

}
