package unittests;

import jcli.annotations.CliOption;
import org.junit.Test;

import java.util.List;

import static jcli.CliParser.parseCommandLineArguments;
import static org.junit.Assert.*;

public class TestListDefaults {

    public static class Args {
        @CliOption(longName = "files", defaultValue = "a.txt, b.txt")
        public List<String> files;
        @CliOption(longName = "empty", defaultValue = "")
        public List<String> empty;
        @CliOption(longName = "names")
        public List<String> names;
        @CliOption(longName = "numbers", defaultValue = "1,2")
        public List<Integer> numbers;
    }

    @Test
    public void commaSeparatedDefaultIsUsed() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{}, Args::new);

        assertEquals(List.of("a.txt", "b.txt"), arguments.files);
    }

    @Test
    public void emptyDefaultIsAnEmptyList() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{}, Args::new);

        assertEquals(List.of(), arguments.empty);
    }

    @Test
    public void withoutADefaultTheListIsEmpty() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{}, Args::new);

        assertEquals(List.of(), arguments.names);
    }

    @Test
    public void defaultsAreConvertedToTheElementType() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{}, Args::new);

        assertEquals(List.of(1, 2), arguments.numbers);
    }

    @Test
    public void commandLineValuesBeatTheDefault() throws Exception {
        final Args arguments = parseCommandLineArguments(new String[]{"--files", "c.txt"}, Args::new);

        assertEquals(List.of("c.txt"), arguments.files);
    }

}
