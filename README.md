
[![Build Status](https://travis-ci.org/codemonstur/jcli.svg?branch=master)](https://travis-ci.org/codemonstur/jcli)
[![GitHub Release](https://img.shields.io/github/release/codemonstur/jcli.svg)](https://github.com/codemonstur/jcli/releases) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.codemonstur/jcli/badge.svg)](http://mvnrepository.com/artifact/com.github.codemonstur/jci)
[![Maintainability](https://api.codeclimate.com/v1/badges/e7d70d686eafc595e04d/maintainability)](https://codeclimate.com/github/codemonstur/jcli/maintainability)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/dwyl/esta/issues)
[![Coverage Status](https://coveralls.io/repos/github/codemonstur/jcli/badge.svg?branch=master)](https://coveralls.io/github/codemonstur/jcli?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1436fca32a66475c8244666f4402b38e)](https://www.codacy.com/manual/codemonstur/jcli?)
[![Sputnik](https://sputnik.ci/conf/badge)](https://sputnik.ci/app#/builds/codemonstur/jcli)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)

# jcli - tinier than picocli

A command line arguments parser.

Getting better and better. 
There are a number of tests and a lot is covered.
This code is close to production ready.
I have started using it here and there.

The project was born, like so many before it, out of frustration with the existing options.

The design philosophy is; make command line argument parsing simple so you can start writing real code. 

## Usage

Include it as a dependency.
Use the builder to parse an class you wrote and decorated with the proper annotations.

```
public static void main(final String.. args) {
    final Arguments arguments = newCliParser(Arguments::new)
        .onErrorPrintHelpAndExit()
        .onHelpPrintHelpAndExit()
        .parseSuppressErrors(args);

    // ... do your stuff
}
```

There are other options and ways to call the parser.
This is so far the cleanest and easiest.
My favorite.

## Future features

There are some ideas listed in `src/main/docs/future_features.md`

