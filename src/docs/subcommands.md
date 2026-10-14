# Subcommands

A parser can dispatch on the first bare token of the command line: `tool add file.txt`
selects the `add` parser and gives it everything after the subcommand name.

## Registering

```java
import jcli.CliParserBuilder;
import jcli.CommandResult;

import static jcli.CliParserBuilder.newCliParser;

final CliParserBuilder<Root> parser = newCliParser(Root::new)
        .name("tool")
        .subcommand("add", "Add a file to the index", newCliParser(Add::new))
        .subcommand("commit", "Record the changes", newCliParser(Commit::new));
```

`subcommand(name, parser)` registers without a description, `subcommand(name, description,
parser)` with one. The description is what the help output and the completion scripts show.

Registration mistakes are `InvalidOptionConfiguration`s: a name that is empty or starts
with `-`, a duplicate name, or a `null` parser.

## Parsing

With subcommands registered you must call `parseWithSubcommands`:

```java
final CommandResult<Root> result = parser.parseWithSubcommands(args);
```

Plain `parse` refuses to guess:

```
This parser has subcommands registered, use parseWithSubcommands instead of parse
```

`CommandResult` holds the arguments of every command along the selected path:

| Method | Returns |
|---|---|
| `getRoot()` | the parsed root arguments |
| `hasSubcommand()` | whether any subcommand was selected |
| `getSubcommand()` | the deepest selected name, or `null` |
| `getPath()` | all selected names in order, e.g. `[remote, add]` |
| `get(Class)` | the parsed arguments of the level whose class matches |

```java
final CommandResult<Root> result = parser.parseWithSubcommands(args);

runRootOptions(result.getRoot());

if (result.hasSubcommand() && "add".equals(result.getSubcommand())) {
    final Add args = result.get(Add.class);
    addToIndex(args.file, args.force);
}
```

`get(Class)` throws `IllegalStateException` when that level was not parsed, so only ask
for a type you know is on the selected path.

## How a subcommand is found

The parser walks the command line from the left. The first token that is not an option
(and not the value of one) selects the subcommand; everything before it belongs to the
root command, everything after belongs to the subcommand.

```text
tool --verbose add -f file.txt
      \___ root ___/ \__ add __/
```

* An unknown option before the subcommand still errors: the root parses strictly.
* After a `--`, the next token is treated as the subcommand name.
* An unknown subcommand name gets a suggestion:

```
'comit' is not a valid command. Did you mean 'commit'?
```

## Rules

* **The root command may not declare positional arguments** when it has subcommands;
  positionals belong to the subcommands. Declaring one is a configuration error.
  (A `List` positional on the root would also swallow the subcommand name.)
* Subcommands **may** declare positionals, and they parse them exactly like any other
  parser does.
* Subcommands may register subcommands of their own. `getPath()` then holds the whole
  chain and `get(Class)` picks the level by its arguments class.
* Each level has its own options, constraints, defaults and help. Options of one level
  are invisible to the others.

## Help and version per level

Configure the sub-parsers like any other builder, including their own name so their help
reads well:

```java
newCliParser(Add::new)
        .name("tool add")
        .onErrorPrintHelpAndExit()
        .onHelpPrintHelpAndExit()
```

```text
Usage: tool add [options] FILE

Options:
   -f --force   optional flag   Overwrite an existing file
   -h --help    optional flag   Prints this help
```

The root's help lists the commands:

```text
Usage: tool [options]

Options:
   -h --help      optional flag   Prints this help
   -v --verbose   optional flag   More output
Commands:
   add      Add a file to the index
   commit   Record the changes
```

Without any `onHelp...` configuration the help flag of a subcommand is just a value in
its arguments object, and you check it yourself through `result.get(Add.class).help`.

## Completion

`CliCompletion.toBash(builder)` (and fish, zsh) understand subcommands and complete the
options of the selected level, see [Shell completion](shell-completion.md).
