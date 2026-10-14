# jcli documentation

jcli is a small command line parser for Java: annotate fields, call `parse`, get your object back.
It has no dependencies and tries to stay out of your way.

Requires Java 17 or newer. Published on Maven Central as `com.github.codemonstur:jcli`.

```java
public class Args {
    @CliOption(name = 'f', longName = "file", isMandatory = true, description = "The file to read")
    public String file;
    @CliOption(name = 'h', longName = "help", isHelp = true, description = "Prints this help")
    public boolean help;
}
```

```java
final Args arguments = newCliParser(Args::new)
        .name("reader")
        .onErrorPrintHelpAndExit()
        .onHelpPrintHelpAndExit()
        .parse(args);
```

## Guides

| Document | Covers |
|---|---|
| [Getting started](getting-started.md) | Dependency, first parser, help, version, errors, exit codes, testing |
| [Annotations](annotations.md) | `@CliOption`, `@CliPositional`, `@CliCommand`: every attribute and the supported types |
| [Command line syntax](command-line-syntax.md) | How tokens are parsed: short groups, attached values, `--`, arity, counting |
| [Defaults and converters](defaults-and-converters.md) | Where values come from when they are not on the command line |
| [Validation](validation.md) | `allowedValues`, `regex`, `min`, `max`, `requires`, `excludes` |
| [Help and localization](help-and-localization.md) | Help output, groups, wrapping, translating every message |
| [Subcommands](subcommands.md) | `parseWithSubcommands`, `CommandResult`, nesting |
| [Shell completion](shell-completion.md) | Completion scripts for bash, fish and zsh |
| [GraalVM native image](native-image.md) | The annotation processor that writes `reflect-config.json` |
| [Migrating to 2.0.0](migration-2.0.0.md) | What changed for existing users |

## Policies

Bugs, feature requests and security reports all go into a GitHub issue.
