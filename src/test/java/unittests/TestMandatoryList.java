package unittests;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMandatoryList {

    public static class MandatoryList {
        @CliOption(longName = "item", isMandatory = true)
        public List<String> items;
    }

    @Test
    public void missingMandatoryListThrows() {
        final String[] args = { };

        assertThrows(InvalidCommandLine.class, () ->
            parseCommandLineArguments(args, MandatoryList::new));
    }

    @Test
    public void mandatoryListWithValues() throws InvalidCommandLine {
        final String[] args = { "--item", "one", "--item", "two" };
        final MandatoryList arguments = parseCommandLineArguments(args, MandatoryList::new);

        assertEquals(2, arguments.items.size(), "List size is wrong");
        assertEquals("one", arguments.items.get(0), "First item is wrong");
        assertEquals("two", arguments.items.get(1), "Second item is wrong");
    }

    @Test
    public void mandatoryListInAttachedForm() throws InvalidCommandLine {
        final String[] args = { "--item=one", "--item=two" };
        final MandatoryList arguments = parseCommandLineArguments(args, MandatoryList::new);

        assertEquals(2, arguments.items.size(), "List size is wrong");
        assertEquals("one", arguments.items.get(0), "First item is wrong");
        assertEquals("two", arguments.items.get(1), "Second item is wrong");
    }

}
