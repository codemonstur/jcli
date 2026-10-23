package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.*;

public class TestRawListOption {

    public static class RawList {
        @SuppressWarnings("rawtypes")
        @CliOption(name = 'a')
        public List list;
    }

    public static class UnsupportedElements {
        @CliOption(name = 'a')
        public List<Map<String, String>> list;
    }

    public static class NestedLists {
        @CliOption(name = 'a')
        public List<List<String>> list;
    }

    @Test
    public void rawListBehavesLikeAListOfStrings() throws InvalidCommandLine {
        final String[] args = { "-a", "one", "-a", "two" };
        final RawList arguments = parseCommandLineArguments(args, RawList::new);

        assertNotNull(arguments.list, "List does not exist");
        assertEquals(2, arguments.list.size(), "List size is wrong");
        assertEquals("one", arguments.list.get(0), "First item is wrong");
        assertEquals("two", arguments.list.get(1), "Second item is wrong");
    }

    @Test
    public void unsupportedElementTypeIsRejected() {
        final String[] args = { "-a", "one" };

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, UnsupportedElements::new));
    }

    @Test
    public void nestedListIsRejected() {
        final String[] args = { "-a", "one" };

        assertThrows(InvalidOptionConfiguration.class, () ->
            parseCommandLineArguments(args, NestedLists::new));
    }

}
