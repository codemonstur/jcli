
Steal some features from here:
https://github.com/h908714124/jbock#features-overview
https://github.com/h908714124/jbock/blob/master/READ_MORE.md

PositionalArguments
Flags are automatic from the selected type (boolean or Boolean)
Support the Optional class

long form is really called attached form

add a map collector

add a lookup DB type thing that maps the argument to something in the DB:
- from a Map<String, Object> instance
- from a Map<String, String> instance
- from a ResourceBundle instance
- from a Properties instance 

escape sequence to disable CliOption's

allowPrefixedPositionalArguments

String[] argv = {"-f hello.txt"};
Parser.newParser()
    .withErrorExitCode(2)                                           // default is 1
    .withErrorStream(new PrintStream(new ByteArrayOutputStream()))  // default is System.err
    .withOutputStream(new PrintStream(new ByteArrayOutputStream())) // default is System.out
    .withIndent(2)                                                  // default is 7
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"))       // default is none
    .parseOrExit(argv);
