
[![GitHub Release](https://img.shields.io/github/v/release/codemonstur/jcli.svg)](https://github.com/codemonstur/jcli/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.codemonstur/jcli.svg)](https://central.sonatype.com/artifact/com.github.codemonstur/jcli)
[![MIT Licence](https://img.shields.io/github/license/codemonstur/jcli.svg)](LICENSE)

# jcli - tiny, simple, gets out of your way

A command line arguments parser.

Requires Java 17 or newer.

Production ready.

The project was born, like so many before it, out of frustration with the existing options.

The design philosophy is; make command line argument parsing simple so you can start writing real code. 

## Usage

Include it as a dependency.

```
<dependency>
    <groupId>com.github.codemonstur</groupId>
    <artifactId>jcli</artifactId>
    <version>2.0.0</version>
</dependency>
```

Use the builder to parse a class you decorated with the proper annotations.

```
public static void main(final String... args) {
    final CliArguments arguments = newCliParser(CliArguments::new)
        .onErrorPrintHelpAndExit()
        .onHelpPrintHelpAndExit()
        .parseSuppressErrors(args);

    // ... do your stuff
}
```

A decorated class looks like this:

```
@CliCommand(name = "command", description =
    "General description of the tool that will show up at the start of the help")
public final class CliArguments {

    @CliOption(name = 'r', longName = "repeatable")
    public List<String> repeatable;
    @CliOption(name = 'o', longName = "output-file", defaultValue = "")
    public String outputFile;

    @CliPositional
    public Action action;

    @CliOption(name = 'h', longName = "help", isHelp = true)
    public boolean help;

}
```

## Features

- Short options in groups and with attached values: `-vq`, `-fdata.txt`, `-f=data.txt`
- A `--` that ends the options, and negative numbers that are values, not options
- Unknown options get a suggestion: `'--flie' is not a valid command line argument. Did you mean '--file'?`
- Errors that name the option and the type that was expected
- `arity`: options that take an explicit `true`/`false`, options that take exactly N values,
  and options that take as many values as you give them
- Counting flags: `-vvv` becomes `3`
- `private` and `final` fields on the arguments class, assigned by the parser through reflection
- `record` argument classes, built through their canonical constructor (`newCliParser(Args.class)`)
- Interactive options that prompt for their value, with a pluggable prompter
- Value validation: `allowedValues`, `regex`, `min`, `max`, and options that `requires` or `excludes` others
- Defaults from the annotation, from a Map, a Properties or a ResourceBundle, from a system property,
  or from an environment variable
- Converters per type (`addClassConverter`) and per field (`fieldConverter`), the field one wins
- A resource bundle that translates the help labels and the error messages, with English fallbacks
- A prompter you can plug in for interactive options (`builder.prompter(...)`)
- `builder.allowPrefixedPositionals()`: unknown option-looking arguments become positionals
- `builder.ignoreMissingMandatory()`: missing mandatory arguments are no longer an error
- `defaultValue` on List options, as a comma separated list
- Subcommands, nested ones too, parsed into a `CommandResult`
- Aliases, hidden options, deprecated options that warn when they are used
- Help with groups, footers, wrapped descriptions, and positionals that document themselves
- The help and error text translated through your own ResourceBundle
- Completion scripts for bash, fish and zsh: `CliCompletion.toBash(MyArgs.class)`
- An annotation processor that writes the reflect-config.json GraalVM native-image needs

## Future features

- Annotation-driven custom converters, declared on the option instead of on the builder:
  either `@CliOption(..., mappedBy = MyMapper.class)` where the parser instantiates a `StringToType`
  mapper class, or a registry of mapper interfaces that turn a `Class<T>` and a String into a T

Please share any ideas you might have.

## Documentation

Guides for every feature live in [src/docs](src/docs/index.md), from
[getting started](src/docs/getting-started.md) over the
[annotation reference](src/docs/annotations.md) to
[migrating from 1.5.2](src/docs/migration-2.0.0.md).
