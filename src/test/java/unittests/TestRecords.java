package unittests;

import jcli.CommandResult;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static jcli.CliParser.parseCommandLineArguments;
import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.*;

public class TestRecords {

    public record FileAndOutput(
            @CliOption(isMandatory = true, longName = "file") String file
            , @CliPositional String output) {}

    public record CountingFlags(
            @CliOption(name = 'v', isCounting = true) int verbose
            , @CliOption(name = 'f', longName = "force") boolean force) {}

    public record ListsAndDefaults(
            @CliOption(longName = "files") List<String> files
            , @CliOption(longName = "count", defaultValue = "3") int count
            , @CliOption(longName = "maybe") Optional<String> maybe) {}

    public record HelpRecord(
            @CliOption(longName = "help", isHelp = true) boolean help
            , @CliOption(isMandatory = true, longName = "file") String file) {}

    public record RootArgs(@CliOption(longName = "verbose") boolean verbose) {}
    public record ChildArgs(@CliOption(longName = "name") String name) {}

    public static class Plain {
        @CliOption(longName = "name") public String name;
    }

    @Test
    public void parsesOptionAndPositionalIntoRecord() throws InvalidCommandLine {
        final FileAndOutput args = parseCommandLineArguments(new String[]{"--file", "in.txt", "out.txt"}
                , FileAndOutput.class);
        assertEquals("in.txt", args.file());
        assertEquals("out.txt", args.output());
    }

    @Test
    public void parsesThroughTheBuilder() throws InvalidCommandLine {
        final CountingFlags args = newCliParser(CountingFlags.class).parse(new String[]{"-vvv", "-f"});
        assertEquals(3, args.verbose());
        assertTrue(args.force());
    }

    @Test
    public void seedsListsDefaultsAndEmptyOptionals() throws InvalidCommandLine {
        final ListsAndDefaults args = newCliParser(ListsAndDefaults.class)
                .parse(new String[]{"--files", "a.txt", "--files", "b.txt"});
        assertEquals(List.of("a.txt", "b.txt"), args.files());
        assertEquals(3, args.count());
        assertEquals(Optional.empty(), args.maybe());
    }

    @Test
    public void missingMandatoryFails() {
        assertThrows(InvalidCommandLine.class, () ->
            newCliParser(FileAndOutput.class).parse(new String[]{"out.txt"}));
    }

    @Test
    public void helpBeatsMandatoryAndIsReadableOnTheRecord() throws InvalidCommandLine {
        final HelpRecord args = newCliParser(HelpRecord.class).parse(new String[]{"--help"});
        assertTrue(args.help());
        assertNull(args.file());
    }

    @Test
    public void plainClassThroughClassMode() throws InvalidCommandLine {
        final Plain args = newCliParser(Plain.class).parse(new String[]{"--name", "x"});
        assertEquals("x", args.name);
    }

    @Test
    public void recordSubcommands() throws InvalidCommandLine {
        final CommandResult<RootArgs> result = newCliParser(RootArgs.class)
                .subcommand("go", newCliParser(ChildArgs.class))
                .parseWithSubcommands(new String[]{"--verbose", "go", "--name", "x"});
        assertTrue(result.getRoot().verbose());
        assertEquals("x", result.get(ChildArgs.class).name());
    }
}
