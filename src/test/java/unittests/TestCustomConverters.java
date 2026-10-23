package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static jcli.CliHelp.getHelp;
import static jcli.CliParser.parseCommandLineArguments;
import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.*;

public class TestCustomConverters {

    public static class Custom {
        public final String value;
        public Custom(final String value) {
            this.value = value;
        }
    }

    public static class Arguments {
        @CliOption(longName = "custom")
        public Custom custom;
        @CliOption(longName = "date")
        public LocalDate date;
    }

    public static class PositionalArguments {
        @CliPositional
        public Custom custom;
    }

    public static class WithoutConverter {
        @CliOption(longName = "custom")
        public Custom custom;
    }

    @Test
    public void customTypeIsConverted() throws InvalidCommandLine {
        final Arguments arguments = newCliParser(Arguments::new).name("test")
            .addClassConverter(Custom.class, Custom::new)
            .parse(new String[]{"--custom", "hello"});

        assertEquals("hello", arguments.custom.value, "The converter was not used");
    }

    @Test
    public void customTypeAsPositionalIsConverted() throws InvalidCommandLine {
        final PositionalArguments arguments = newCliParser(PositionalArguments::new).name("test")
            .addClassConverter(Custom.class, Custom::new)
            .parse(new String[]{"hello"});

        assertEquals("hello", arguments.custom.value, "The converter was not used");
    }

    @Test
    public void registeredConverterOverridesTheBuiltInOne() throws InvalidCommandLine {
        final Arguments arguments = newCliParser(Arguments::new).name("test")
            .addClassConverter(Custom.class, Custom::new)
            .addClassConverter(LocalDate.class, value -> LocalDate.MIN)
            .parse(new String[]{"--custom", "hello", "--date", "2020-01-01"});

        assertEquals(LocalDate.MIN, arguments.date, "The registered converter was ignored");
    }

    @Test
    public void helpAcceptsConvertedTypes() {
        final String help = getHelp("test", Arguments.class, Collections.singleton(Custom.class));

        assertTrue(help.contains("--custom"), "The help does not describe the custom option");
    }

    @Test
    public void typeWithoutAConverterIsRejected() {
        final String[] args = { "--custom", "hello" };

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, WithoutConverter::new));
    }

}
