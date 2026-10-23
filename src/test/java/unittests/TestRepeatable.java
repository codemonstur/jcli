package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestRepeatable {

    public static class StringArguments {
        @CliOption(name = 'a')
        public List<String> list;
    }

    @Test
    public void twoListStrings() throws InvalidCommandLine {
        final String[] args = { "-a", "something", "-a", "more" };
        final StringArguments arguments = parseCommandLineArguments(args, StringArguments::new);

        assertNotNull(arguments.list, "List does not exist");
        assertEquals(2, arguments.list.size(), "List sizes are not equal");
        assertEquals("something", arguments.list.get(0), "Item 1 is invalid");
        assertEquals("more", arguments.list.get(1), "Item 2 is invalid");
    }

    public static class IntegerArguments {
        @CliOption(name = 'a')
        public List<Integer> list;
    }

    @Test
    public void twoListIntegers() throws InvalidCommandLine {
        final String[] args = { "-a", "1", "-a", "10" };
        final IntegerArguments arguments = parseCommandLineArguments(args, IntegerArguments::new);

        assertNotNull(arguments.list, "List does not exist");
        assertEquals(2, arguments.list.size(), "List sizes are not equal");
        assertEquals(1, (int) arguments.list.get(0), "Item 1 is invalid");
        assertEquals(10, (int) arguments.list.get(1), "Item 2 is invalid");
    }

}
