# Help and localization

## Producing help

Three ways, from most configured to least:

```java
// 1. through the builder: name, indent, width, resource bundle and subcommands all apply
newCliParser(Args::new)
        .name("backup")
        .resourceBundle(bundle)
        .helpWidth(80)
        .onHelpPrintHelpAndExit()      // prints and exits 1
        .parse(args);

// 2. the same text, as a string, with the builder's settings
final String help = newCliParser(Args::new).name("backup").resourceBundle(bundle).getHelpText();

// 3. directly from the class, with default settings
System.out.println(CliHelp.getHelp(Args.class));
CliHelp.printHelp("backup", Args.class);
```

The static `CliHelp` overloads are `getHelp(Class)`, `getHelp(name, Class)`,
`getHelp(name, Class, convertedTypes)`, `getHelp(name, indent, Class)` and
`getHelp(name, indent, Class, convertedTypes)`; `printHelp` prints them.
They do not know about your resource bundle, width or subcommands — use
`getHelpText()` or the builder's help flow for that.

Builder settings that shape the output:

| Method | Default | Effect |
|---|---|---|
| `name(String)` | none | The command name in the usage line and examples. Falls back to `@CliCommand.name`, then nothing. |
| `indent(String)` | `"   "` | The indent of every row. |
| `helpWidth(int)` | `0` (off) | Wrap descriptions at this column. |
| `resourceBundle(bundle)` | English | Translate labels and messages. |
| `helpConsumer(...)` | `System.out::println` | Where the printed help goes. |
| `errorConsumer(...)` | `System.err::println` | Where the `[ERROR]` line of `onErrorPrintHelpAndExit` goes. |
| `helpExitCode(int)` | `1` | The exit code of `onHelpPrintHelpAndExit`. |

## What the output looks like

Given this class:

```java
@CliCommand(name = "backup", description = "A tiny backup tool", footer = "MIT licensed"
        , examples = {"-d /srv data", "--include '*.sql' data"})
public class Sample {

    @CliOption(name = 'd', longName = "directory", isMandatory = true
            , description = "The directory to back up")
    public String directory;

    @CliOption(name = 'v', longName = "verbose", isCounting = true
            , description = "More output, repeat for more")
    public int verbose;

    @CliOption(longName = "include", defaultValue = "*.log"
            , description = "Glob patterns to include")
    public List<String> include;

    @CliOption(name = 'q', longName = "quiet", group = "Output", description = "No output")
    public boolean quiet;

    @CliOption(name = 'h', longName = "help", isHelp = true, description = "Prints this help")
    public boolean help;

    @CliPositional(name = "TARGET", description = "Where to write the backup")
    public String target;
}
```

`CliHelp.getHelp(Sample.class)` prints:

```text
A tiny backup tool

Examples:
   backup -d /srv data
   backup --include '*.sql' data

Usage: backup [options] TARGET

Options:
   -d --directory   mandatory value        The directory to back up
   -v --verbose     optional  flag         More output, repeat for more
   --include        optional  value        Glob patterns to include [default: *.log]
   -h --help        optional  flag         Prints this help
   TARGET           mandatory positional   Where to write the backup
Output:
   -q --quiet       optional  flag         No output

MIT licensed
```

Section by section:

1. `@CliCommand.description`
2. `Examples:` — one line per entry, prefixed with the command name
3. `Usage: <name> [options] <positional names>`
4. `Options:` — every non-hidden option and every positional, in declaration order
5. one section per `group`, in the order the groups first appear, under their own heading
6. `Commands:` — the registered [subcommands](subcommands.md), if any
7. `@CliCommand.footer`

Each row is: names (`-d --directory`, `-v`, `TARGET`), then `mandatory`/`optional`
followed by `flag`, `value` or `positional`, then the description. The description ends
with ` [deprecated]` for deprecated options and `[default: x]` when a default exists.

Hidden options (`isHidden`) never appear. Deprecated ones appear with their marker and a
warning whenever they are used on the command line.

With `helpWidth(80)` long descriptions wrap onto continuation lines aligned under the
description column; words longer than the width are left alone.

## Localization

```java
newCliParser(Args::new)
        .resourceBundle(ResourceBundle.getBundle("messages"))
        .onErrorPrintHelpAndExit()
        .onHelpPrintHelpAndExit()
        .parse(args);
```

The same bundle translates three things: the help labels, the error messages thrown by
the parser, and the deprecation warnings.

* A bundle can be **partial**: any key you do not provide falls back to the built in
  English text.
* Bundle values are formatted with `MessageFormat`, so the placeholders are `{0}`, `{1}`,
  ... and a literal single quote must be doubled (`''`). The English defaults shown below
  are already formatted when they are built.
* `InvalidCommandLine` re-reads the bundle in `getMessage()`, so the message is
  translated at the moment you ask for it.

### Help keys

| Key | English | Params |
|---|---|---|
| `help.usage` | `Usage:` | |
| `help.options` | `Options:` | |
| `help.commands` | `Commands:` | |
| `help.examples` | `Examples:` | |
| `help.mandatory` | `mandatory` | |
| `help.optional` | `optional` | |
| `help.flag` | `flag` | |
| `help.value` | `value` | |
| `help.positional` | `positional` | |
| `help.deprecated` | `[deprecated]` | |
| `help.default` | `[default: %s]` | `{0}` default value |

### Error keys

| Key | English | Params |
|---|---|---|
| `error.unknownArgument` | `'%s' is not a valid command line argument` | `{0}` argument |
| `error.singleDash` | `Long form arguments must start with two dashes, '%s' does not` | `{0}` argument |
| `error.argumentTooShort` | `A command line option must have a name, '-' or '--' is not valid` | |
| `error.notACommandLineArgument` | `Command line arguments must start with '-', %s does not` | `{0}` argument |
| `error.missingValue` | `Argument '%s' is missing a value` | `{0}` argument |
| `error.duplicateArgument` | `'%s' was provided more than once` | `{0}` argument |
| `error.missingArgument` | `Missing argument '%s'` | `{0}` argument |
| `error.missingPositional` | `Missing positional argument %s` | `{0}` field name |
| `error.tooManyPositionals` | `You have provided too many positional arguments. There are %s but you have provided %s` | `{0}` total, `{1}` given |
| `error.flagAttached` | `Flag arguments do not use attached long form` | |
| `error.invalidValue` | `The command line value '%s' has the wrong format` | `{0}` value |
| `error.invalidValueFor` | `The value '%s' for '%s' is not a valid %s` | `{0}` value, `{1}` argument, `{2}` type |
| `error.invalidBoolean` | `The value '%s' for '%s' must be true or false` | `{0}` value, `{1}` argument |
| `error.disallowedValue` | `The value '%s' for '%s' is not allowed, it must be one of: %s` | `{0}` value, `{1}` argument, `{2}` allowed |
| `error.invalidRegexValue` | `The value '%s' for '%s' does not match the pattern %s` | `{0}` value, `{1}` argument, `{2}` pattern |
| `error.tooSmall` | `The value '%s' for '%s' must be at least %s` | `{0}` value, `{1}` argument, `{2}` min |
| `error.tooLarge` | `The value '%s' for '%s' must be at most %s` | `{0}` value, `{1}` argument, `{2}` max |
| `error.missingArityValues` | `The option '%s' expects %s values but only %s were given` | `{0}` argument, `{1}` expected, `{2}` given |
| `error.requires` | `The option '%s' requires '%s'` | `{0}` argument, `{1}` required |
| `error.excludes` | `The option '%s' cannot be used together with '%s'` | `{0}` argument, `{1}` excluded |
| `error.unknownSubcommand` | `'%s' is not a valid command` | `{0}` subcommand |
| `error.promptFailed` | `Could not read a value for '%s'` | `{0}` argument |
| `error.didYouMean` | `Did you mean '%s'?` | `{0}` suggestion |
| `warning.deprecated` | `The option '%s' is deprecated` | `{0}` argument |

### A bundle

```properties
help.usage=Verwendung:
help.options=Optionen:
help.commands=Befehle:
help.mandatory=Pflicht
help.optional=Optional
help.flag=Schalter
help.value=Wert
help.positional=Positional
error.missingValue=Argument ''{0}'' hat keinen Wert
error.unknownArgument=''{0}'' ist kein gültiges Argument
error.didYouMean=Meintest du ''{0}''?
warning.deprecated=Die Option ''{0}'' ist veraltet
```

Everything you leave out stays English.

## Deprecation warnings

Using an option marked `isDeprecated` warns on stderr by default. Redirect or silence it
with:

```java
newCliParser(Args::new)
        .deprecationConsumer(message -> log.warn(message))
        .parse(args);
```
