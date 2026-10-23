# Getting started

## The dependency

```xml
<dependency>
    <groupId>com.github.codemonstur</groupId>
    <artifactId>jcli</artifactId>
    <version>2.0.0</version>
</dependency>
```

```gradle
implementation 'com.github.codemonstur:jcli:2.0.0'
```

jcli has no runtime dependencies and needs Java 17 or newer.

## Your first parser

Describe the command line with annotations on the fields of a plain class:

```java
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

public class Args {

    @CliOption(name = 'f', longName = "file", isMandatory = true
            , description = "The file to read")
    public String file;

    @CliOption(name = 'v', longName = "verbose", isCounting = true
            , description = "More output, repeat for more")
    public int verbose;

    @CliOption(name = 'h', longName = "help", isHelp = true
            , description = "Prints this help")
    public boolean help;

    @CliPositional(name = "OUTPUT", description = "Where to write the result")
    public String output;
}
```

The fields may be `private` and can be `final`: the parser assigns them through
reflection after construction. A final field must be assigned in the constructor, not with an
inline constant initializer — a field like `final int verbose = 0;` is inlined
at every read site by the compiler, and reads would never see the parsed value.
A `record` works too, created with the class instead of a Supplier:
`newCliParser(Args.class)` stages the values and calls the canonical constructor.
A component that the parser does not fill gets `null`/`false`/`0`; compute a
default in a compact constructor if you need one.

Parse the command line with a builder:

```java
import jcli.errors.InvalidCommandLine;

import static jcli.CliParserBuilder.newCliParser;

public static void main(final String[] args) throws InvalidCommandLine {
    final Args arguments = newCliParser(Args::new)
            .name("reader")
            .onErrorPrintHelpAndExit()
            .onHelpPrintHelpAndExit()
            .parse(args);

    if (!arguments.help) run(arguments);
}
```

That single `.parse(args)` gives you every behaviour in the table of contents:
short groups (`-fv`), attached values (`-ffile.txt`), counting (`-vvv`), defaults,
validation, interactive prompts, and errors that suggest the option you meant.

## The three exits

The builder decides what happens when parsing fails, when help is requested and when
version is requested. Each has a print-and-exit shortcut and a callback:

```java
newCliParser(Args::new)
        .name("reader")
        // print '[ERROR] <message>' to stderr and the help text to stdout, then exit 2
        .onErrorPrintHelpAndExit()
        // print the help text, then exit 1
        .onHelpPrintHelpAndExit()
        // print the version (from the jar manifest), then exit 3
        .onVersionPrintVersionAndExit()
        .parse(args);
```

Default exit codes and where the text goes:

| Path | Default code | Output | Change it with |
|---|---|---|---|
| Error | `2` | `[ERROR] message` on stderr, help text on stdout | `errorExitCode(int)`, `onErrorCall(...)`, `errorConsumer(...)` |
| Help | `1` | help text on stdout | `helpExitCode(int)`, `helpConsumer(...)`, `onHelpCall(...)` |
| Version | `3` | `<name> v<version>` on stdout | `versionExitCode(int)`, `versionConsumer(...)`, `onVersionCall(...)` |

The callbacks and the print-and-exit shortcuts are independent, and they can be combined:

```java
.onErrorCall((error, args) -> log.warn("bad arguments: {}", error.getMessage()))
.onErrorPrintHelpAndExit()
```

### What parse throws

`parse` declares one checked exception, `InvalidCommandLine`. Configuration mistakes
(for example `isHelp` on a non-boolean field) are programming errors and surface as the
unchecked `InvalidOptionConfiguration` from `jcli.errors`.

If you would rather handle errors yourself, do not configure any of the `on...` methods
and catch the exception:

```java
try {
    final Args arguments = newCliParser(Args::new).parse(args);
    run(arguments);
} catch (final InvalidCommandLine e) {
    System.err.println(e.getMessage());
    System.err.println(CliHelp.getHelp(Args.class));
    System.exit(2);
}
```

`parseSuppressErrors(args)` is the quick variant: it returns `null` instead of throwing.

## The version flag

Annotate a boolean field with `isVersion` and configure the exit behaviour once:

```java
@CliOption(name = 'V', longName = "version", isVersion = true, description = "Prints the version")
public boolean version;
```

```java
newCliParser(Args::new)
        .name("reader")
        .onVersionPrintVersionAndExit()
        .parse(args);
```

The printed title and version come from the jar manifest (`ImplementationTitle`,
`ImplementationVersion`), falling back to the name you gave the builder.

## Testing without System.exit

`System.exit` kills the test JVM, so the builder takes an `exitHandler`:

```java
final List<Integer> exits = new ArrayList<>();

final Args arguments = newCliParser(Args::new)
        .name("reader")
        .onErrorPrintHelpAndExit()
        .onHelpPrintHelpAndExit()
        .exitHandler(exits::add)
        .parse(new String[]{"--help"});
```

When the handler returns:

* the help and version paths continue normally and `parse` returns the arguments object,
* the error path rethrows the `InvalidCommandLine` after the handler ran.

The default handler is `System::exit`.

## Where the parser looks for values

Everything about the fallback sources, precedence and converters is in
[Defaults and converters](defaults-and-converters.md). The short version, highest
priority first:

1. the command line,
2. a system property, then an environment variable named in the annotation,
3. the `defaultsFrom(...)` map,
4. `defaultValue` of the annotation.

A value from any of those sources satisfies an `isMandatory` option; an option is only
"missing" when no source can provide it.

## Next steps

* Every attribute in [Annotations](annotations.md)
* What the parser accepts on the command line in [Command line syntax](command-line-syntax.md)
* The full help output in [Help and localization](help-and-localization.md)
