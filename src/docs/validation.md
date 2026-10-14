# Validation

Values are converted first, then checked. Every failure names the option, the value and
what was expected.

```java
public class Args {

    @CliOption(longName = "mode", allowedValues = {"read", "write"}, description = "The access mode")
    public String mode;

    @CliOption(longName = "port", min = "1", max = "65535", description = "The port to bind")
    public int port;

    @CliOption(longName = "host", regex = "[a-z]+\\.[a-z]+", description = "The host to connect to")
    public String host;

    @CliOption(longName = "log", requires = "verbose", description = "Log to a file")
    public String log;

    @CliOption(longName = "verbose", description = "Print more")
    public boolean verbose;

    @CliOption(longName = "quiet", excludes = "verbose", description = "Print nothing")
    public boolean quiet;
}
```

## allowedValues

The raw string on the command line must be one of the listed values:

```text
$ tool --mode delete
The value 'delete' for '--mode' is not allowed, it must be one of: read, write
```

The comparison happens before conversion, so `allowedValues` also works as an enum
sub-selection on a field whose type is not a String.

## regex

The raw string must match the whole value (`String.matches`):

```text
$ tool --host Example
The value 'Example' for '--host' does not match the pattern [a-z]+\.[a-z]+
```

The pattern must compile; a broken one is a configuration error raised before parsing.

## min and max

Bounds apply to numeric fields and compare the converted number, so they work on
`int`, `long`, `double`, `BigDecimal` and the rest:

```text
$ tool --port 0
The value '0' for '--port' must be at least 1

$ tool --port 70000
The value '70000' for '--port' must be at most 65535
```

Declaring `min`/`max` on a non-numeric field, or a `min` greater than the `max`, is a
configuration error.

## requires

When the option is used, every named option must have been used too:

```text
$ tool --log app.log
The option '--log' requires '--verbose'
```

## excludes

When the option is used, none of the named options may be used:

```text
$ tool --quiet --verbose
The option '--quiet' cannot be used together with '--verbose'
```

Names in `requires`/`excludes` refer to the options of the same command by their long or
short name. A name that matches nothing is a configuration error, not a silent no-op.

## Order and scope

* Conversion runs before the value checks, so `--port abc` reports the type error, not
  the range.
* `requires`/`excludes` are checked once, after the whole command line parsed
  successfully, and only between options that were actually used.
* `--help` and `--version` skip the relation and mandatory checks; the user asked for
  help, they get help.
* Positional arguments carry no constraints of their own; they only convert.
* A custom converter replaces the conversion but not the checks: `allowedValues`,
  `regex`, `min` and `max` still run on its result where they make sense.
