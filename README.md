
[![Build Status](https://travis-ci.org/codemonstur/jcli.svg?branch=master)](https://travis-ci.org/codemonstur/jcli)
[![GitHub Release](https://img.shields.io/github/release/codemonstur/jcli.svg)](https://github.com/codemonstur/jcli/releases) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.codemonstur/jcli/badge.svg)](http://mvnrepository.com/artifact/com.github.codemonstur/jcli)
[![Maintainability](https://api.codeclimate.com/v1/badges/63924c44946973cb37f8/maintainability)](https://codeclimate.com/github/codemonstur/jcli/maintainability)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/dwyl/esta/issues)
[![Coverage Status](https://coveralls.io/repos/github/codemonstur/jcli/badge.svg?branch=master)](https://coveralls.io/github/codemonstur/jcli?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/813d8482256b4ed88e2ff1018d53f06e)](https://www.codacy.com/app/codemonstur/jcli)
[![Sputnik](https://sputnik.ci/conf/badge)](https://sputnik.ci/app#/builds/codemonstur/jcli)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)

# jcli - tiny, simple, gets out of your way

A command line arguments parser.

Production ready.

The project was born, like so many before it, out of frustration with the existing options.

The design philosophy is; make command line argument parsing simple so you can start writing real code. 

## Usage

Include it as a dependency.
Use the builder to parse a class you decorated with the proper annotations.

```
public static void main(final String.. args) {
    final Arguments arguments = newCliParser(Arguments::new)
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

    @CliPositional(index = 0)
    public Action action;

    @CliOption(name = 'h', longName = "help", isHelp = true)
    public boolean help;

}
```

## Future features

There are some ideas listed in `src/main/docs/future_features.md`

