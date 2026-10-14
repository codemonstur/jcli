# Migrating to 2.0.0

## Requirements

* **Java 17 or newer** to compile against jcli (1.5.2 ran on Java 8). That break is what
  the major version is for: 1.5.2 stays the last release on Maven Central for Java 8
  users.
* Nothing else changed for consumers: same coordinates, same packages, no transitive
  dependencies.

```xml
<version>2.0.0</version>
```

## Source compatibility

Nothing was removed. Every public method, annotation attribute and class that existed in
1.5.2 still exists with the same signature; 2.0.0 only adds:

* new `@CliOption` attributes (`arity`, `isCounting`, `isInteractive`, `aliases`,
  `isHidden`, `isDeprecated`, `group`, `allowedValues`, `regex`, `min`, `max`,
  `requires`, `excludes`, `systemProperty`, `environmentVariable`)
* `name` and `description` on `@CliPositional`, `footer` on `@CliCommand`
* builder methods: `exitHandler`, `helpWidth`, `resourceBundle`, `prompter`,
  `ignoreMissingMandatory`, `allowPrefixedPositionals`, `defaultsFrom`, `errorConsumer`,
  `deprecationConsumer`, `fieldConverter`, `subcommand`, `getSubcommandDescriptions`,
  `getHelpText`, `parseWithSubcommands`
* new classes: `CommandResult`, `CliCompletion`, `CliPrompter`, `ConsolePrompter`,
  `Messages`, `CliReflectionProcessor`

Your 1.5.2 code compiles unchanged.

## Behaviour you might notice

These are deliberate fixes; tests that assert exact text or old errors need re-baselining.

| In 1.5.2 | In 2.0.0 |
|---|---|
| `Missing argument 'file'` | `Missing argument '--file'` (names the actual token) |
| `The command line value 'abc' has the wrong format` | `The value 'abc' for '--count' is not a valid Integer` (names the option and the type) |
| unknown option: `'--flie' is not a valid command line argument` | same, plus `. Did you mean '--file'?` when a close match exists |
| the same option given twice silently took the last value | `'--file' was provided more than once` |
| `--help` could still fail with `Missing argument ...` | `--help` and `--version` short-circuit missing-mandatory and `requires`/`excludes` errors |
| a mandatory option failed even when it had a `defaultValue` | the default resolves the option; only when no source has a value is it missing |
| `defaultValue` on a `List` option was ignored | applied, comma separated (`""` means empty list) |
| `-file` errored with `Long form arguments must start with two dashes, '-file' does not` | the same error, plus `. Did you mean '--file'?` |
| `Usage: test [options] \n` (trailing space) | `Usage: test [options]\n` |
| `-5` was an unknown option | negative numbers are values |

New syntax that used to be an error and now parses:

```text
-freport.txt          attached short value
-vq                   group of short options
-vfreport.txt         group ending in a value-taking option
--                    ends the options, the rest are positionals
```

And new escape hatches for things that had no workaround:

* `allowPrefixedPositionals()` for values that look like options,
* `ignoreMissingMandatory()` when missing arguments should not fail the parse.

## New capabilities

Everything from the author's old roadmap lists, all implemented in 2.0.0:

* defaults from a `Map`, `Properties` or `ResourceBundle`, from a system property or an
  environment variable (`defaultsFrom`, `systemProperty`, `environmentVariable`)
* `--` end-of-options escape
* `allowPrefixedPositionals`
* a `ResourceBundle` for help text and error messages
* `Boolean` fields with an explicit value (`arity = 1`, `--force=false`)
* interactive options with a pluggable prompter
* counting flags (`-vvv` → `3`)
* richer help: groups, footer, wrapping, positional documentation
* `arity` (`N` values, or `-1` for as many as given)
* the GraalVM native-image annotation processor
* plus: subcommands, value constraints, per-field converters, shell completion,
  aliases/hidden/deprecated options, exit handlers for tests, and did-you-mean
  suggestions.

Start at the [documentation index](index.md) for the guided tour.

## Building jcli from source

Only relevant if you build the library itself:

* Maven **3.6.3 or newer** (enforced).
* `mvn verify` runs the tests; the license and CVE checks are opt-in profiles:
  `mvn verify -P licenses,security`.
* `mvn deploy` needs Maven 3.9+ and a `central` server entry in `settings.xml`.
