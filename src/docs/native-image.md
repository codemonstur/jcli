# GraalVM native image

jcli reads your arguments classes through reflection. In a native image reflection must
be declared ahead of time, and jcli ships an annotation processor that writes that
declaration while your project compiles.

## What it does

Every compilation that has jcli on the classpath runs `jcli.CliReflectionProcessor`
(it is registered in `META-INF/services/javax.annotation.processing.Processor`). For
every field annotated with `@CliOption` or `@CliPositional` it collects the class and
the field, and for every enum used as a field type it collects the enum. At the end of
the compilation it writes:

```text
META-INF/native-image/jcli/reflect-config.json
```

into the compilation output, next to your classes. That lands in your jar, and
`native-image` picks up everything under `META-INF/native-image/` on its classpath
automatically — no extra flags.

The generated file looks like:

```json
[
  {"name":"com.example.Args","fields":[{"name":"file"},{"name":"verbose"},{"name":"target"}]},
  {"name":"com.example.Mode","allDeclaredFields":true}
]
```

Enums get `allDeclaredFields` because the parser resolves values with `Enum.valueOf`.

## Usage

Nothing to configure. Example build:

```bash
mvn package
native-image -jar target/myapp.jar        # reflect-config.json travels inside the jar
```

Or with classes:

```bash
javac -d out $(find src -name '*.java')
native-image -cp out:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout) com.example.Main
```

## Running the processor by hand

Useful for checking the output without a full build, exactly what the test suite does:

```bash
javac -proc:only -processor jcli.CliReflectionProcessor \
      -classpath jcli-2.0.0.jar \
      -d out \
      src/main/java/com/example/Args.java
```

## Turning it off

The processor costs a little time on every compilation. If you do not build native
images, disable it:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <proc>none</proc>
    </configuration>
</plugin>
```

or pass `-proc:none` to `javac`.

## Limits

* The processor only sees the classes compiled in that round. If your arguments classes
  live in a library, that library must ship its own `reflect-config.json` (built with the
  same processor), or you declare the classes yourself in a `reflect-config.json` passed
  to `native-image`.
* Keep `@CliOption` fields non-final and public as always: the generated entries mirror
  exactly the fields the parser will set at runtime.
