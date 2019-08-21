
Steal some features from here:
https://github.com/h908714124/jbock#features-overview

String[] argv = {"-f hello.txt"};
Parser.newParser()
    .withErrorExitCode(2)                                           // default is 1
    .withErrorStream(new PrintStream(new ByteArrayOutputStream()))  // default is System.err
    .withOutputStream(new PrintStream(new ByteArrayOutputStream())) // default is System.out
    .withIndent(2)                                                  // default is 7
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"))       // default is none
    .parseOrExit(argv);
