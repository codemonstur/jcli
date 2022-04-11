package example;

import jcli.CliParserBuilder;
import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;

import static jcli.CliParserBuilder.newCliParser;

public class BuilderExample {

    public static class Arguments {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
    }

    public static void main(final String... args) throws InvalidCommandLine {
        exampleTwo();
//        printHelp("example", Arguments.class);
    }

    public static void exampleOne(final String... args) throws InvalidCommandLine {
        final Arguments arguments = new CliParserBuilder<Arguments>()
            .name("test")
            .object(Arguments::new)
            .onErrorPrintHelpAndExit()
            .onHelpPrintHelpAndExit()
            .parse(args);

        // do stuff
    }

    public static void exampleTwo(final String... args) throws InvalidCommandLine {
        final Arguments arguments = newCliParser(Arguments::new)
            .name("test")
            .onErrorPrintHelpAndExit()
            .onHelpPrintHelpAndExit()
            .parse(args);

        // do stuff
    }

    public static void exampleThree(final String... args) throws InvalidCommandLine {
        final Arguments arguments = CliParserBuilder.<Arguments>newCliParser()
            .object(Arguments::new)
            .name("test")
            .onErrorPrintHelpAndExit()
            .onHelpPrintHelpAndExit()
            .parse(args);

        // do stuff
    }

    public static void exampleFour(final String... args) {
        final Arguments arguments = newCliParser(Arguments::new)
            .name("test")
            .onErrorPrintHelpAndExit()
            .onHelpPrintHelpAndExit()
            .parseSuppressErrors(args);

        // do stuff
    }
}
