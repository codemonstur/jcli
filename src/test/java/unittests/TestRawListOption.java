package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import jcli.errors.InvalidOptionConfiguration;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

        assertNotNull("List does not exist", arguments.list);
        assertEquals("List size is wrong", 2, arguments.list.size());
        assertEquals("First item is wrong", "one", arguments.list.get(0));
        assertEquals("Second item is wrong", "two", arguments.list.get(1));
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void unsupportedElementTypeIsRejected() throws InvalidCommandLine {
        final String[] args = { "-a", "one" };
        parseCommandLineArguments(args, UnsupportedElements::new);
    }

    @Test(expected = InvalidOptionConfiguration.class)
    public void nestedListIsRejected() throws InvalidCommandLine {
        final String[] args = { "-a", "one" };
        parseCommandLineArguments(args, NestedLists::new);
    }

}
