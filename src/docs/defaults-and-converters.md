# Defaults and converters

Where a value comes from when it is not on the command line, and how strings become
objects.

## Precedence

For every option that is absent from the command line, the sources are consulted in this
order, and the first hit wins:

1. **the command line** (including a value read by an interactive prompt)
2. **`systemProperty`** of the annotation, a JVM property
3. **`environmentVariable`** of the annotation, a process environment variable
4. **`defaultsFrom(...)`** the map or bundle given to the builder
5. **`defaultValue`** of the annotation

```java
@CliOption(longName = "mode", systemProperty = "app.mode", environmentVariable = "APP_MODE"
        , defaultValue = "safe")
public String mode;
```

```java
// --mode fast       beats everything
// -Dapp.mode=fast   beats the environment, the map and the annotation
// APP_MODE=fast     beats the map and the annotation
// defaultsFrom(Map.of("mode", "fast"))   beats the annotation
// otherwise         "safe"
```

## Configured defaults

```java
newCliParser(Args::new)
        .defaultsFrom(Map.of("config", "/etc/app.conf", "level", "3"))
        .parse(args);
```

`defaultsFrom` accepts:

* a `Map<?, ?>` (a `Properties` instance is one), keys and values are read as strings,
* a `ResourceBundle`, for properties-file style configuration.

Keys are looked up per option in this order: the long name, the short name, then the
field name. Positionals are looked up by their display name (`name` of the annotation,
or the field name) and then by the field name. The last `defaultsFrom` call replaces the
previous one.

Positionals do not have `systemProperty`/`environmentVariable`; the map and
`defaultValue` are their only fallback sources.

## Annotation defaults

```java
@CliOption(longName = "pattern", defaultValue = "*.log")
public List<String> pattern;
```

* On a `List` the default is comma separated, and each element is trimmed:
  `"a, b, c"` becomes `[a, b, c]`. An empty string means an empty list.
* Defaults are converted and validated like command line values: a broken default is
  reported with the same error that names the option and the type.
* A `List` option that appears on the command line gets exactly what the command line
  says; the default is not merged in.
* A counting flag starts at `0`, or at its numeric `defaultValue`.

### Help-only defaults

```java
@CliOption(longName = "threads", defaultValue = "4", setDefaultWhenMissing = false)
public Integer threads;
```

The help output still says `[default: 4]`, but your code gets `null` (or
`Optional.empty()`, or `0` for a primitive) unless the user provides the option. Use this
when the default belongs in the documentation but your code must distinguish "asked for
the default" from "given the default".

Configured defaults (`systemProperty`, `environmentVariable`, `defaultsFrom`) still apply
when `setDefaultWhenMissing` is false: it only suppresses the annotation's own
`defaultValue`.

## Mandatory and defaults

`isMandatory` is satisfied by any source. Only when no source could provide a value does
the parser fail:

```
Missing argument '--file'
```

```java
// works: the property resolves the mandatory option
@CliOption(longName = "config", isMandatory = true, systemProperty = "APP_CONFIG")
public String config;
```

To allow mandatory fields to stay empty altogether:

```java
newCliParser(Args::new).ignoreMissingMandatory().parse(args);
```

With that setting missing mandatory options and positionals are left at their initial
value (`null`, `Optional.empty()`, `0`) instead of failing. Defaults that do exist are
still applied.

## Converters

Two hooks turn strings into objects. Lookup order for every value, including defaults:

1. a **field converter** registered for that field name,
2. a **class converter** registered for a class assignable to the target type,
3. the **built-in conversion** for the supported types.

```java
import jcli.Reflection.StringToType;

final Args arguments = newCliParser(Args::new)
        .addClassConverter(Host.class, value -> Host.parse(value))
        .fieldConverter("ports", value -> parsePorts(value))
        .parse(args);
```

Both hooks take a `StringToType<T>`:

```java
public interface StringToType<T> {
    T toType(String value);
}
```

### Class converters

`addClassConverter(Class<C>, converter)` adds a conversion for a type. It also makes that
type a valid field type, so a field of an otherwise unsupported class passes
configuration validation:

```java
@CliOption(longName = "host")   // Host is not a JDK type
public Host host;
```

For `List<C>` fields the converter runs once per element.

### Field converters

`fieldConverter(fieldName, converter)` is keyed by the Java field name and beats
everything else for that one field. It accepts *any* field type without further
validation, so it is the escape hatch for types nothing else can convert:

```java
@CliOption(longName = "range")   // an unsupported record, converted by the field converter
public IntRange range;
```

```java
.fieldConverter("range", value -> IntRange.parse(value))
```

### Converter errors

When a converter throws (anything, including `RuntimeException`s like
`NumberFormatException`), the parser reports it against the option and the expected type:

```
The value 'oops' for '--count' is not a valid Integer
```

The original exception is attached as the cause, so `error.getCause()` still has it.
