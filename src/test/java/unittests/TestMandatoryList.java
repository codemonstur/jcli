package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.assertEquals;

public class TestMandatoryList {

    public static class MandatoryList {
        @CliOption(longName = "item", isMandatory = true)
        public List<String> items;
    }

    @Test(expected = InvalidCommandLine.class)
    public void missingMandatoryListThrows() throws InvalidCommandLine {
        final String[] args = { };
        parseCommandLineArguments(args, MandatoryList::new);
    }

    @Test
    public void mandatoryListWithValues() throws InvalidCommandLine {
        final String[] args = { "--item", "one", "--item", "two" };
        final MandatoryList arguments = parseCommandLineArguments(args, MandatoryList::new);

        assertEquals("List size is wrong", 2, arguments.items.size());
        assertEquals("First item is wrong", "one", arguments.items.get(0));
        assertEquals("Second item is wrong", "two", arguments.items.get(1));
    }

    @Test
    public void mandatoryListInAttachedForm() throws InvalidCommandLine {
        final String[] args = { "--item=one", "--item=two" };
        final MandatoryList arguments = parseCommandLineArguments(args, MandatoryList::new);

        assertEquals("List size is wrong", 2, arguments.items.size());
        assertEquals("First item is wrong", "one", arguments.items.get(0));
        assertEquals("Second item is wrong", "two", arguments.items.get(1));
    }

}
