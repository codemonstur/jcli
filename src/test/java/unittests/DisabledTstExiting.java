package unittests;

public class DisabledTstExiting {
    // FIXME these tests work fine but bob can't resolve the dependency due to it
    //  using a version range. SO for now I've disabled the test and removed the dependency
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
