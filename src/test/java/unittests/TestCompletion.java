package unittests;

import jcli.CliCompletion;
import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import org.junit.Test;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

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

        assertTrue(script, script.contains("# bash completion for tool"));
        assertTrue(script, script.contains("tool_completions()"));
        assertTrue(script, script.contains("-f --file -h --help"));
        assertTrue(script, script.contains("complete -F tool_completions tool"));
        assertTrue("Positionals complete as files:\n" + script, script.contains("compgen -f"));
    }

    @Test
    public void fishListsTheOptionsWithDescriptions() {
        final String script = CliCompletion.toFish(Args.class);

        assertTrue(script, script.contains("complete -c tool -s f -l file -r -F -d 'The file to read'"));
        assertTrue(script, script.contains("complete -c tool -s h -l help -d 'Prints the help'"));
    }

    @Test
    public void zshListsTheOptionsWithDescriptions() {
        final String script = CliCompletion.toZsh(Args.class);

        assertTrue(script, script.contains("#compdef tool"));
        assertTrue(script, script.contains("'-f[The file to read]:file:_files'"));
        assertTrue(script, script.contains("'-h[Prints the help]'"));
    }

    @Test
    public void subcommandsAppearInTheBashScript() {
        final String script = CliCompletion.toBash(newCliParser(Root::new).name("tool")
                .subcommand("build", "Build it", newCliParser(Build::new)));

        assertTrue(script, script.contains("case \"$command\" in"));
        assertTrue(script, script.contains("build)"));
        assertTrue(script, script.contains("-v --verbose build"));
    }

    @Test
    public void subcommandsAppearInTheFishScript() {
        final String script = CliCompletion.toFish(newCliParser(Root::new).name("tool")
                .subcommand("build", "Build it", newCliParser(Build::new)));

        assertTrue(script, script.contains("complete -c tool -a build -d 'Build it'"));
        assertTrue(script, script.contains("-n 'not __fish_seen_subcommand_from build' -l output"));
    }

    @Test
    public void subcommandsAppearInTheZshScript() {
        final String script = CliCompletion.toZsh(newCliParser(Root::new).name("tool")
                .subcommand("build", "Build it", newCliParser(Build::new)));

        assertTrue(script, script.contains("'1:command:->commands'"));
        assertTrue(script, script.contains("compadd build"));
        assertTrue(script, script.contains("'--output[Where to build]:output:_files'"));
    }

}
