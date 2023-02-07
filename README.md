
[![GitHub Release](https://img.shields.io/github/release/codemonstur/jcli.svg)](https://github.com/codemonstur/jcli/releases) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.codemonstur/jcli/badge.svg)](http://mvnrepository.com/artifact/com.github.codemonstur/jcli)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)

# jcli - tiny, simple, gets out of your way

A command line arguments parser.

Production ready.

The project was born, like so many before it, out of frustration with the existing options.

The design philosophy is; make command line argument parsing simple so you can start writing real code. 

## Usage

Include it as a dependency.

```
<dependency>
    <groupId>com.github.codemonstur</groupId>
    <artifactId>jcli</artifactId>
    <version>1.4.0</version>
</dependency>
```

Use the builder to parse a class you decorated with the proper annotations.

```
public static void main(final String.. args) {
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

## Future features

There are some ideas listed in [future features](https://github.com/codemonstur/jcli/blob/master/src/docs/future_features.md)

