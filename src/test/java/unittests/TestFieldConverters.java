package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.*;

public class TestFieldConverters {

    public static class Custom {
        public final String value;
        public Custom(final String value) {
            this.value = value;
        }
    }

    public static class Args {
        @CliOption(name = 'h', longName = "help", isHelp = true)
        public boolean help;
        @CliOption(longName = "host")
        public Custom host;
        @CliOption(longName = "date")
        public LocalDate date;
        @CliOption(longName = "birthday")
        public LocalDate birthday;
        @CliPositional
        public Custom source;
    }

    public static class OnlyCustom {
        @CliOption(longName = "host")
        public Custom host;
    }

    @Test
    public void fieldConverterConvertsAnUnsupportedType() throws Exception {
        final Args arguments = newCliParser(Args::new)
                .fieldConverter("host", Custom::new)
                .fieldConverter("source", Custom::new)
                .parse(new String[]{"--host", "example.com", "some-path"});

        assertEquals("example.com", arguments.host.value);
        assertEquals("some-path", arguments.source.value);
    }

    @Test
    public void typeWithoutAFieldConverterIsRejected() {
        try {
            newCliParser(OnlyCustom::new).parse(new String[]{});
            fail("Parsing should have failed on the unsupported type");
        } catch (final Exception e) {
            assertInstanceOf(InvalidOptionConfiguration.class, e, "Wrong exception thrown: " + e);
            assertEquals("Option 'host' has an invalid type", e.getMessage());
        }
    }

    @Test
    public void fieldConverterOverridesTheDefaultConversion() throws Exception {
        final Args arguments = newCliParser(Args::new)
                .fieldConverter("host", Custom::new)
                .fieldConverter("source", Custom::new)
                .fieldConverter("birthday", value -> LocalDate.parse(value
                        , java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .parse(new String[]{"--date", "2024-01-02", "--birthday", "03/04/2024", "src"});

        assertEquals(LocalDate.of(2024, 1, 2), arguments.date, "The built in conversion should be untouched");
        assertEquals(LocalDate.of(2024, 4, 3), arguments.birthday, "The field converter should win");
    }

    @Test
    public void fieldConverterBeatsClassConverter() throws Exception {
        final Args arguments = newCliParser(Args::new)
                .fieldConverter("host", Custom::new)
                .fieldConverter("source", Custom::new)
                .addClassConverter(LocalDate.class, value -> LocalDate.MIN)
                .fieldConverter("birthday", LocalDate::parse)
                .parse(new String[]{"--date", "2024-01-02", "--birthday", "2024-04-03", "src"});

        assertEquals(LocalDate.MIN, arguments.date, "The class converter applies where no field converter is registered");
        assertEquals(LocalDate.of(2024, 4, 3), arguments.birthday, "The field converter applies to its own field");
    }

    @Test
    public void aConverterThatBlowsUpNamesTheField() {
        try {
            newCliParser(Args::new)
                .fieldConverter("host", value -> {
                    throw new IllegalStateException("boom");
                })
                .fieldConverter("source", Custom::new)
                .parse(new String[]{"--host", "x", "src"});
            fail("Parsing should have failed on the converter error");
        } catch (final Exception e) {
            assertInstanceOf(InvalidCommandLine.class, e, "Wrong exception thrown: " + e);
            assertEquals("The value 'x' for '--host' is not a valid Custom", e.getMessage());
        }
    }

    @Test
    public void helpAcceptsFieldConvertedTypes() throws Exception {
        final StringBuilder help = new StringBuilder();

        newCliParser(Args::new)
            .fieldConverter("host", Custom::new)
            .fieldConverter("source", Custom::new)
            .onHelpPrintHelpAndExit().exitHandler(code -> { }).helpConsumer(help::append)
            .parse(new String[]{"-h"});

        assertTrue(help.toString().contains("--host"), "The help does not describe the converted option:\n" + help);
    }

}
