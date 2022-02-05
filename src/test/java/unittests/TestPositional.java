package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.*;
import org.junit.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.*;

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

        assertEquals("The l argument is not equal", 2, arguments.l);
        assertEquals("The file argument is not equal", "somefile.txt", arguments.file);
    }

    @Test
    public void defaultForPositional() throws InvalidCommandLine {
        final String[] args = { };
        final OptionAndPositional arguments = parseCommandLineArguments(args, OptionAndPositional::new);

        assertEquals("The l argument is not equal", 1, arguments.l);
        assertEquals("The file argument is not equal", "file.txt", arguments.file);
    }


    @Test(expected = InvalidCommandLine.class)
    public void oneMandatoryNotThere() throws InvalidCommandLine {
        final String[] args = {};
        parseCommandLineArguments(args, OneMandatory::new);

        fail("MissingArgument not thrown");
    }

    @Test
    public void oneMandatoryProvided() throws InvalidCommandLine {
        final String[] args1 = { "hello" };
        final OneMandatory arguments1 = parseCommandLineArguments(args1, OneMandatory::new);

        assertNotNull("First is null", arguments1.first);
        assertEquals("First is invalid", "hello", arguments1.first);
        assertEquals("Second is invalid", "second", arguments1.second);

        final String[] args2 = { "hello", "world" };
        final OneMandatory arguments2 = parseCommandLineArguments(args2, OneMandatory::new);

        assertEquals("Second is invalid", "world", arguments2.second);
    }

    @Test(expected = InvalidPositionalConfiguration.class)
    public void mandatoryAfterOptional() throws InvalidCommandLine {
        final String[] args = { "hello" };
        parseCommandLineArguments(args, MandatoryAfterOptional::new);

        fail("MandatoryAfterOptional not thrown");
    }

    @Test(expected = InvalidCommandLine.class)
    public void tooManyPositionalArguments() throws InvalidCommandLine {
        final String[] args = { "hello", "one-too-many" };
        parseCommandLineArguments(args, OptionAndPositional::new);

        fail("TooManyPositionalArguments not thrown");
    }

    @Test
    public void hasEmptyPostionalList() throws InvalidCommandLine {
        final String[] args = { };
        final ListWithoutType arguments = parseCommandLineArguments(args, ListWithoutType::new);

        assertNotNull("Positionals list is null", arguments.positionals);
        assertEquals("Positional length is invalid", 0, arguments.positionals.size());
    }

    @Test
    public void hasMultiplePositionals() throws InvalidCommandLine {
        final String[] args = { "hello", "another-one" };
        final ListWithoutType a2 = parseCommandLineArguments(args, ListWithoutType::new);

        assertNotNull("Positional list is null", a2.positionals);
        assertEquals("Positional length is invalid", 2, a2.positionals.size());
        assertEquals("First positional is invalid", "hello", a2.positionals.get(0));
        assertEquals("Second positional is invalid", "another-one", a2.positionals.get(1));

        final ListOfStrings a3 = parseCommandLineArguments(args, ListOfStrings::new);
        assertNotNull("Positional list is null", a3.positionals);
        assertEquals("Positional length is invalid", 2, a3.positionals.size());
        assertEquals("First positional is invalid", "hello", a3.positionals.get(0));
        assertEquals("Second positional is invalid", "another-one", a3.positionals.get(1));
    }

    @Test
    public void hasMultipleIntegerPositionals() throws InvalidCommandLine {
        final String[] args = { "1", "2" };
        final ListOfIntegers arguments = parseCommandLineArguments(args, ListOfIntegers::new);

        assertNotNull("Positional list is null", arguments.positionals);
        assertEquals("Positional length is invalid", 2, arguments.positionals.size());
        assertEquals("First positional is invalid", Integer.valueOf(1), arguments.positionals.get(0));
        assertEquals("Second positional is invalid", Integer.valueOf(2), arguments.positionals.get(1));
    }

    @Test(expected = InvalidPositionalConfiguration.class)
    public void invalidPositionalConfigurationFails() throws InvalidCommandLine {
        final String[] args = { "1", "2" };
        parseCommandLineArguments(args, InvalidListConfig::new);

        fail("DefaultForListPositional not thrown");
    }
}
