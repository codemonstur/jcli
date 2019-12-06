package unit;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.*;

public class TestRepeatable {

    public static class StringArguments {
        @CliOption(name = 'a')
        public List<String> list;
    }

    @Test
    public void twoListStrings() throws InvalidCommandLine {
        final String[] args = { "-a", "something", "-a", "more" };
        final StringArguments arguments = parseCommandLineArguments(args, StringArguments::new);

        assertNotNull("List does not exist", arguments.list);
        assertEquals("List sizes are not equal", arguments.list.size(), 2);
        assertEquals("Item 1 is invalid", "something", arguments.list.get(0));
        assertEquals("Item 2 is invalid", "more", arguments.list.get(1));
    }

    public static class IntegerArguments {
        @CliOption(name = 'a')
        public List<Integer> list;
    }

    @Test
    public void twoListIntegers() throws InvalidCommandLine {
        final String[] args = { "-a", "1", "-a", "10" };
        final IntegerArguments arguments = parseCommandLineArguments(args, IntegerArguments::new);

        assertNotNull("List does not exist", arguments.list);
        assertEquals("List sizes are not equal", arguments.list.size(), 2);
        assertEquals("Item 1 is invalid", 1, (int) arguments.list.get(0));
        assertEquals("Item 2 is invalid", 10, (int) arguments.list.get(1));
    }

}
