# Annotations

Everything jcli knows about your command line comes from three annotations.

## @CliOption

Goes on a field and describes one option.

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `name` | `char` | `' '` | The short name: `-f`. At least one of `name`/`longName` is required. |
| `longName` | `String` | `""` | The long name: `--file` (the dashes are not part of the value). |
| `description` | `String` | `""` | One line shown in the help output. |
| `isMandatory` | `boolean` | `false` | The option must resolve to a value, from the command line or from any [default source](defaults-and-converters.md). `--help` short-circuits the check. |
| `defaultValue` | `String` | none | The value used when nothing else provides one. Comma separated for `List` fields, an empty string means an empty list. |
| `setDefaultWhenMissing` | `boolean` | `true` | Set to `false` to show `defaultValue` in the help without actually giving it to your code. |
| `isHelp` | `boolean` | `false` | This flag requests help. Must be a boolean field, cannot combine with `isVersion` or an `arity`. |
| `isVersion` | `boolean` | `false` | This flag prints the version. Same rules as `isHelp`. |
| `arity` | `int` | `0` | How many values the option consumes, see [arity](#arity) below. |
| `isCounting` | `boolean` | `false` | Count occurrences instead of setting a value: `-vvv` becomes `3`, see [counting flags](#counting-flags). |
| `isInteractive` | `boolean` | `false` | When the option is given without a value, read the value from a prompt, see [interactive options](#interactive-options). |
| `aliases` | `String[]` | `{}` | Extra names for the same option. One character becomes another short name, anything else another long name. |
| `isHidden` | `boolean` | `false` | Keep the option out of the help output. It still parses. |
| `isDeprecated` | `boolean` | `false` | Mark it `[deprecated]` in the help and warn (on stderr) whenever it is used. |
| `group` | `String` | `""` | Section heading in the help output. Empty means the default section. |
| `allowedValues` | `String[]` | `{}` | The raw value must be one of these strings, see [Validation](validation.md). |
| `regex` | `String` | `""` | The raw value must match this pattern. |
| `min` | `String` | `""` | Smallest allowed value, numeric fields only. |
| `max` | `String` | `""` | Largest allowed value, numeric fields only. |
| `requires` | `String[]` | `{}` | Options that must also be present when this one is used. |
| `excludes` | `String[]` | `{}` | Options that must not be present when this one is used. |
| `systemProperty` | `String` | `""` | Fallback: read this JVM system property when the option is not on the command line. |
| `environmentVariable` | `String` | `""` | Fallback: read this environment variable when the option is not on the command line. |

```java
@CliOption(name = 'o', longName = "out", aliases = {"overwrite"}, group = "Output"
        , description = "Where to write the report", defaultValue = "report.txt")
public String output;
```

## @CliPositional

Goes on a field and describes a positional argument, one of the bare tokens left over
after the options.

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `name` | `String` | `""` | The label in the help output and the key in the `defaultsFrom` map. Defaults to the field name. |
| `description` | `String` | `""` | One line shown in the help output. |
| `defaultValue` | `String` | none | Applied when the token is absent. A positional with a default is shown as `optional`, without one as `mandatory`. |

```java
@CliPositional(name = "OUTPUT", description = "Where to write the result", defaultValue = "-")
public String output;
```

Positionals do not have `systemProperty`/`environmentVariable`; their fallback sources are
the `defaultsFrom` map and `defaultValue`.

## @CliCommand

Goes on the class and describes the program itself. It feeds the help output and the
completion scripts.

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `name` | `String` | `""` | The command name in the usage line, the examples and the completion script. The builder's `.name(...)` wins when both are set; otherwise the class name is used. |
| `description` | `String` | `""` | The first line of the help output. |
| `examples` | `String[]` | `{}` | Shown in an `Examples:` section, each prefixed with the command name. |
| `footer` | `String` | `""` | Printed below the options and commands, one use per line. |

```java
@CliCommand(name = "backup", description = "A tiny backup tool"
        , examples = {"-d /srv data", "--dry-run --verbose"}, footer = "MIT licensed")
public class Args { ... }
```

## Field rules

* Fields must be `public` and non-static. They must not be `final`: the parser sets them
  through reflection.
* An option needs `name`, `longName` or both. With both, either form on the command line works.
* Two fields may not share a name (including aliases).
* Fields that are not annotated are ignored.

## Supported types

| Group | Types |
|---|---|
| Text | `String` |
| Numbers | `int`/`Integer`, `long`/`Long`, `short`/`Short`, `byte`/`Byte`, `float`/`Float`, `double`/`Double`, `BigDecimal`, `BigInteger` |
| Flags | `boolean`/`Boolean` |
| Characters | `char`/`Character` |
| Files and ids | `File`, `Path`, `URI`, `Pattern`, `Charset` |
| Time | `Instant`, `LocalTime`, `LocalDate`, `LocalDateTime`, `OffsetDateTime`, `ZonedDateTime` |
| Optionals | `Optional<T>`, `OptionalInt`, `OptionalLong`, `OptionalDouble` (always initialized, empty when no value is found) |
| Enums | any enum. Values that contain `_` can be written with a dash on the command line by putting `@CliUnderscoreIsDash` on the enum type. |
| Lists | `List<T>` where `T` is any of the above |
| Anything else | a class registered with `addClassConverter(...)`, or a field registered with `fieldConverter(...)` |

A value that does not convert names the option and the expected type:

```
The value 'abc' for '--count' is not a valid Integer
```

## Arity

`arity` says how many values one occurrence of the option consumes.

| Arity | Behaviour |
|---|---|
| `0` (default) | Infer from the type: booleans are flags that take no value, everything else takes one value. |
| `1` | Exactly one value. On a `Boolean` field this makes `--force=false` work instead of treating the option as a flag. |
| `> 1` | Exactly that many values in one occurrence. Requires a `List` field. |
| `-1` | As many values as you give it, until the next option-looking token. Requires a `List` field. |

```java
@CliOption(longName = "force", arity = 1)           // --force true | --force=false
public Boolean force;

@CliOption(longName = "pair", arity = 2)            // --pair a b  (exactly two)
public List<String> pair;

@CliOption(longName = "files", arity = -1)          // --files a b c
public List<String> files;
```

Not enough values is an error that names the expectation:

```
The option '--pair' expects 2 values but only 1 were given
```

## Counting flags

`isCounting` turns a flag into a counter. Requires an `int`/`Integer` or `long`/`Long`
field. A counting flag:

* cannot be `isMandatory` (it always has a value),
* cannot declare an `arity`,
* starts at `0`, or at a numeric `defaultValue` if you set one,
* is repeatable: `-vvv` gives `3`.

```java
@CliOption(name = 'v', longName = "verbose", isCounting = true, defaultValue = "1")
public int verbosity;
```

## Interactive options

`isInteractive` prompts for the value when the option appears on the command line
without one:

```
Enter a value for --password: 
```

It only applies to options that take a value: a boolean field must declare
`arity = 1`. The prompt comes from the builder's `prompter`, by default
`ConsolePrompter`, which reads without echoing when a console is attached and falls
back to stdin otherwise. A cancelled prompt (end of input) fails with:

```
Could not read a value for '--password'
```

An interactive option that never appears on the command line is not prompted for; it
falls through to the regular [default sources](defaults-and-converters.md).

## Help and version flags

```java
@CliOption(name = 'h', longName = "help", isHelp = true, description = "Prints this help")
public boolean help;

@CliOption(name = 'V', longName = "version", isVersion = true, description = "Prints the version")
public boolean version;
```

Rules enforced at configuration time: the field must be boolean, a field may not be both
`isHelp` and `isVersion`, and neither may declare an `arity`.

When either flag is set, the parser skips the missing-mandatory and relation checks and
returns the arguments object; the builder's `onHelp...`/`onVersion...` settings then print
and exit. The default callbacks return normally, so this works as a normal return path too:

```java
final Args arguments = newCliParser(Args::new).parse(args);
if (arguments.help) { System.out.println(CliHelp.getHelp(Args.class)); return; }
```

## Configuration errors

Bad annotations are programming errors. The unchecked `InvalidOptionConfiguration` is
thrown the first time the class is parsed or its help is built:

| Message mentions | Trigger |
|---|---|
| `isHelp set on field that is not a boolean` | `isHelp`/`isVersion` on a non-boolean field |
| `isHelp and isVersion both set on a field` | both on one field |
| `declared final` / `declared static` | the field modifier is not allowed |
| `must set either a name or a long name` | neither `name` nor `longName` |
| `used by more than one field` | two fields share a name or alias |
| `has an invalid type` | type is not supported and has no converter |
| `declares an arity of ... which requires a List field` | `arity > 1` or `-1` on a non-list |
| `is a counting flag and cannot declare an arity` / `cannot be mandatory` | counting combined with `arity`/`isMandatory` |
| `requires an int, Integer, long or Long field` | counting on another type |
| `with a defaultValue that is not a number` | counting `defaultValue` is not parseable |
| `is interactive but is a flag` | interactive boolean without `arity = 1` |
| `declares min ... and/or max ... which is not valid for a numeric field` | `min`/`max` on a non-numeric field |
| `declares a regex that does not compile` | broken `regex` |
| `refers to ... in its requires/excludes, but no option with that name exists` | unknown name in `requires`/`excludes` |
| `declares the empty alias` | `aliases = {""}` |
