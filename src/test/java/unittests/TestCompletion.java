package unittests;

import jcli.CliCompletion;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import org.junit.jupiter.api.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCompletion {

    @CliCommand(name = "tool")
    public static class Args {
        @CliOption(name = 'f', longName = "file", description = "The file to read")
        public String file;
        @CliOption(name = 'h', longName = "help", isHelp = true, description = "Prints the help")
        public boolean help;
        @CliPositional
        public String action;
    }

    public static class Root {
        @CliOption(name = 'v', longName = "verbose", description = "More output")
        public boolean verbose;
    }

    public static class Build {
        @CliOption(longName = "output", description = "Where to build")
        public String output;
    }

    @Test
    public void bashListsTheOptions() {
        final String script = CliCompletion.toBash(Args.class);

        assertTrue(script.contains("# bash completion for tool"), script);
        assertTrue(script.contains("tool_completions()"), script);
        assertTrue(script.contains("-f --file -h --help"), script);
        assertTrue(script.contains("complete -F tool_completions tool"), script);
        assertTrue(script.contains("compgen -f"), "Positionals complete as files:\n" + script);
    }

    @Test
    public void fishListsTheOptionsWithDescriptions() {
        final String script = CliCompletion.toFish(Args.class);

        assertTrue(script.contains("complete -c tool -s f -l file -r -F -d 'The file to read'"), script);
        assertTrue(script.contains("complete -c tool -s h -l help -d 'Prints the help'"), script);
    }

    @Test
    public void zshListsTheOptionsWithDescriptions() {
        final String script = CliCompletion.toZsh(Args.class);

        assertTrue(script.contains("#compdef tool"), script);
        assertTrue(script.contains("'-f[The file to read]:file:_files'"), script);
        assertTrue(script.contains("'-h[Prints the help]'"), script);
    }

    @Test
    public void subcommandsAppearInTheBashScript() {
        final String script = CliCompletion.toBash(newCliParser(Root::new).name("tool")
                .subcommand("build", "Build it", newCliParser(Build::new)));

        assertTrue(script.contains("case \"$command\" in"), script);
        assertTrue(script.contains("build)"), script);
        assertTrue(script.contains("-v --verbose build"), script);
    }

    @Test
    public void subcommandsAppearInTheFishScript() {
        final String script = CliCompletion.toFish(newCliParser(Root::new).name("tool")
                .subcommand("build", "Build it", newCliParser(Build::new)));

        assertTrue(script.contains("complete -c tool -a build -d 'Build it'"), script);
        assertTrue(script.contains("-n 'not __fish_seen_subcommand_from build' -l output"), script);
    }

    @Test
    public void subcommandsAppearInTheZshScript() {
        final String script = CliCompletion.toZsh(newCliParser(Root::new).name("tool")
                .subcommand("build", "Build it", newCliParser(Build::new)));

        assertTrue(script.contains("'1:command:->commands'"), script);
        assertTrue(script.contains("compadd build"), script);
        assertTrue(script.contains("'--output[Where to build]:output:_files'"), script);
    }

}
