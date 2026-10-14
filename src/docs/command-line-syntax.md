# Command line syntax

This page is the grammar of what the parser accepts, with the exact errors it produces.

## Long options

```text
--file report.txt        separated value
--file=report.txt        attached value
--verbose                a flag takes no value
--                       everything after this is a positional argument
```

`--name` needs a value unless it is a flag. When the next token exists and does not look
like an option it is the value; otherwise the parser reports:

```
Argument '--file' is missing a value
```

A flag may not carry an attached value:

```
Flag arguments do not use attached long form
```

Writing `--force=false` is not the attached form of a flag: it works only when the option
declares `arity = 1` (see [annotations](annotations.md#arity)), which turns the boolean
into a value-taking option.

## Short options

```text
-f report.txt            separated value
-freport.txt             the rest of the token is the value
-f=report.txt            attached with equals
-vq                      a group of flags
-vfreport.txt            group: flags, then the value-taking option eats the rest
-vf report.txt           group: the value is the next token
```

Inside a group every letter must be a known short option. The first option that takes a
value consumes the remainder of the token as its value (`-vfreport.txt` gives `v` the flag
and `f` the value `report.txt`); if the remainder is empty the next token is the value.

Unknown letters and unknown long names get a suggestion when one is close enough:

```
'--flie' is not a valid command line argument. Did you mean '--file'?
```

A single dash in front of what is really a long name is a common typo:

```
Long form arguments must start with two dashes, '-file' does not. Did you mean '--file'?
```

Very short names (one character) are never "corrected": `-f` is always taken literally.

## Positional arguments

Every token that is not an option goes to the `@CliPositional` fields, in declaration
order. Extra tokens are an error:

```
You have provided too many positional arguments. There are 1 but you have provided 2
```

A missing one (with no default) fails with:

```
Missing positional argument source
```

Negative numbers are values, not options: `-5` and `-1.5` are consumed as the value of the
preceding option or as a positional argument.

### Option-looking positionals

By default a token that looks like an option must be one. When your values may start with
a dash, enable the escape hatch:

```java
newCliParser(Args::new).allowPrefixedPositionals().parse(args);
```

Now an unknown `--whatever` becomes the next positional argument instead of an error.
Known options keep parsing as options.

## Counting flags

```text
-v -v -v        3
-vvv            3
```

Counting flags are the exception to the duplicate rule: they may appear any number of
times, and so may `List` options, which accumulate instead:

```text
--tag a --tag b      the list holds [a, b]
```

Every other option given twice fails:

```
'--file' was provided more than once
```

## Arity

| Declared | On the command line |
|---|---|
| default, boolean field | flag: `--verbose`, grouped: `-v` |
| `arity = 1`, boolean field | value: `--force true`, `--force=false` |
| `arity = 2` (list) | `--pair a b`, or `--pair=a,b` |
| `arity = -1` (list) | `--files a b c`, stops at the next option-looking token |

Too few values names the expectation:

```
The option '--pair' expects 2 values but only 1 were given
```

For an attached list value (`--pair=a,b`) the count is checked against the split values,
with an empty value meaning an empty list.

## Interactive prompts

An `isInteractive` option given without a value prompts instead of failing:

```
Enter a value for --password: 
```

The default prompter reads from the console without echoing, or from stdin when there is
no console. End of input is an error:

```
Could not read a value for '--password'
```

## Mandatory arguments

`isMandatory` means "no source could provide a value". The check runs after the whole
command line was parsed and after every [default source](defaults-and-converters.md) was
consulted:

```
Missing argument '--file'
```

`--help` and `--version` short-circuit the check: the user asked for help, do not argue
with them about missing arguments. `ignoreMissingMandatory()` turns the check off
entirely.

## Duplicate names, aliases and deprecation

* Two fields may not share a name or alias; that is a configuration error.
* Aliases parse exactly like the primary names.
* Using an option marked `isDeprecated` prints a warning to stderr (configurable with
  `deprecationConsumer`) but parses normally.

## Order of checks

For a single token the parser reports the first problem it finds:

1. is the token an option at all (unknown option, bad group, single-dash typo),
2. is a value available (missing value, prompt),
3. does the value convert (type error),
4. does the value pass its constraints (`allowedValues`, `regex`, `min`, `max`).

After the whole command line is parsed:

5. `requires` and `excludes` between the options that were used,
6. `--help`/`--version` short-circuit,
7. missing mandatory options and positionals, then their defaults.

## All the messages

| Situation | Message |
|---|---|
| unknown option | `'--flie' is not a valid command line argument. Did you mean '--file'?` |
| single-dash long name | `Long form arguments must start with two dashes, '-file' does not. Did you mean '--file'?` |
| missing value | `Argument '--file' is missing a value` |
| value on a flag | `Flag arguments do not use attached long form` |
| duplicate option | `'--file' was provided more than once` |
| missing mandatory option | `Missing argument '--file'` |
| missing positional | `Missing positional argument source` |
| too many positionals | `You have provided too many positional arguments. There are 1 but you have provided 2` |
| bad conversion | `The value 'abc' for '--count' is not a valid Integer` |
| bad boolean value | `The value 'maybe' for '--force' must be true or false` |
| disallowed value | `The value 'delete' for '--mode' is not allowed, it must be one of: read, write` |
| regex mismatch | `The value 'Example' for '--host' does not match the pattern [a-z]+\.[a-z]+` |
| below minimum | `The value '0' for '--port' must be at least 1` |
| above maximum | `The value '70000' for '--port' must be at most 65535` |
| too few arity values | `The option '--pair' expects 2 values but only 1 were given` |
| missing required option | `The option '--log' requires '--verbose'` |
| excluded combination | `The option '--quiet' cannot be used together with '--verbose'` |
| cancelled prompt | `Could not read a value for '--password'` |
| unknown subcommand | `'chekout' is not a valid command. Did you mean 'checkout'?` |

Every one of these keys can be translated, see
[Help and localization](help-and-localization.md#error-keys).
