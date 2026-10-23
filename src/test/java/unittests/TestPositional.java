package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestPositional {

    public static class OptionAndPositional {
        @CliOption(name = 'l', defaultValue = "1")
        public long l;
        @CliPositional(defaultValue = "file.txt")
        public String file;
    }
    public static class OneMandatory {
        @CliPositional
        public String first;
        @CliPositional(defaultValue = "second")
        public String second;
    }
    @SuppressWarnings("unused")
    public static class MandatoryAfterOptional {
        @CliPositional
        public String first;
        @CliPositional(defaultValue = "second")
        public String second;
        @CliPositional
        public String third;
    }
    public static class ListWithoutType {
        @SuppressWarnings("rawtypes")
        @CliPositional
        public List positionals;
    }
    public static class ListOfStrings {
        @CliPositional
        public List<String> positionals;
    }
    public static class ListOfIntegers {
        @CliPositional
        public List<Integer> positionals;
    }
    @SuppressWarnings("unused")
    public static class InvalidListConfig {
        @SuppressWarnings("rawtypes")
        @CliPositional(defaultValue = "something")
        public List positionals;
    }

    @Test
    public void optionalAndPositionalProvided() throws InvalidCommandLine {
        final String[] args = { "-l", "2", "somefile.txt"};
        final OptionAndPositional arguments = parseCommandLineArguments(args, OptionAndPositional::new);

        assertEquals(2, arguments.l, "The l argument is not equal");
        assertEquals("somefile.txt", arguments.file, "The file argument is not equal");
    }

    @Test
    public void defaultForPositional() throws InvalidCommandLine {
        final String[] args = { };
        final OptionAndPositional arguments = parseCommandLineArguments(args, OptionAndPositional::new);

        assertEquals(1, arguments.l, "The l argument is not equal");
        assertEquals("file.txt", arguments.file, "The file argument is not equal");
    }


    @Test
    public void oneMandatoryNotThere() {
        final String[] args = {};

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, OneMandatory::new));
    }

    @Test
    public void oneMandatoryProvided() throws InvalidCommandLine {
        final String[] args1 = { "hello" };
        final OneMandatory arguments1 = parseCommandLineArguments(args1, OneMandatory::new);

        assertNotNull(arguments1.first, "First is null");
        assertEquals("hello", arguments1.first, "First is invalid");
        assertEquals("second", arguments1.second, "Second is invalid");

        final String[] args2 = { "hello", "world" };
        final OneMandatory arguments2 = parseCommandLineArguments(args2, OneMandatory::new);

        assertEquals("world", arguments2.second, "Second is invalid");
    }

    @Test
    public void mandatoryAfterOptional() {
        final String[] args = { "hello" };

        assertThrows(InvalidPositionalConfiguration.class, () ->
            parseCommandLineArguments(args, MandatoryAfterOptional::new));
    }

    @Test
    public void tooManyPositionalArguments() {
        final String[] args = { "hello", "one-too-many" };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, OptionAndPositional::new));
    }

    @Test
    public void hasEmptyPostionalList() throws InvalidCommandLine {
        final String[] args = { };
        final ListWithoutType arguments = parseCommandLineArguments(args, ListWithoutType::new);

        assertNotNull(arguments.positionals, "Positionals list is null");
        assertEquals(0, arguments.positionals.size(), "Positional length is invalid");
    }

    @Test
    public void hasMultiplePositionals() throws InvalidCommandLine {
        final String[] args = { "hello", "another-one" };
        final ListWithoutType a2 = parseCommandLineArguments(args, ListWithoutType::new);

        assertNotNull(a2.positionals, "Positional list is null");
        assertEquals(2, a2.positionals.size(), "Positional length is invalid");
        assertEquals("hello", a2.positionals.get(0), "First positional is invalid");
        assertEquals("another-one", a2.positionals.get(1), "Second positional is invalid");

        final ListOfStrings a3 = parseCommandLineArguments(args, ListOfStrings::new);
        assertNotNull(a3.positionals, "Positional list is null");
        assertEquals(2, a3.positionals.size(), "Positional length is invalid");
        assertEquals("hello", a3.positionals.get(0), "First positional is invalid");
        assertEquals("another-one", a3.positionals.get(1), "Second positional is invalid");
    }

    @Test
    public void hasMultipleIntegerPositionals() throws InvalidCommandLine {
        final String[] args = { "1", "2" };
        final ListOfIntegers arguments = parseCommandLineArguments(args, ListOfIntegers::new);

        assertNotNull(arguments.positionals, "Positional list is null");
        assertEquals(2, arguments.positionals.size(), "Positional length is invalid");
        assertEquals(Integer.valueOf(1), arguments.positionals.get(0), "First positional is invalid");
        assertEquals(Integer.valueOf(2), arguments.positionals.get(1), "Second positional is invalid");
    }

    @Test
    public void invalidPositionalConfigurationFails() {
        final String[] args = { "1", "2" };

        assertThrows(InvalidPositionalConfiguration.class, () ->
            parseCommandLineArguments(args, InvalidListConfig::new));
    }
}
