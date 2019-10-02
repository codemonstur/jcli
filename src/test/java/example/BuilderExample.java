package example;

import jcli.CliOption;
import jcli.CliParserBuilder;
import jcli.errors.InvalidCommandLine;

public class BuilderExample {

    public static class Arguments {
        @CliOption(longName = "file", isMandatory = true)
        public String file;
    }

    public static void main(final String... args) throws InvalidCommandLine {
        final Arguments arguments = new CliParserBuilder<Arguments>()
            .name("test")
            .object(Arguments::new)
            .onErrorPrintHelpAndExit()
            .onHelpPrintHelpAndExit()
            .parse(args);

        // do stuff
    }

}
