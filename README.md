# jcli

A command line arguments parser.

There is no help and several features are missing.
There are also limited tests at the moment.
This code is not production ready.
But if you feel adventurous feel free to use it.

The project was born, like so many before it, out of frustration with the existing options.

The design philosophy is; make command line argument parsing simple so you can start writing real code. 

When it is done I want the code to look something like this:
```
public static void main(final String.. args) {
    try {
        final Arguments arguments = parseCommandLineArguments(args, Arguments::new);
        if (arguments.help) System.exit(printCliHelp());

        // ... do your stuff
    } catch (InvalidCommandLine e) {
        printCliHelpWithError(e, Arguments.class);
    }
}
```
Or perhaps like this:
```
public static void main(final String.. args) {
    try {
        final Arguments arguments = newCliParser()
            .object(Arguments::new)
            .addClassConvertor(Something.class, StringToSomething)
            .addFieldConvertor(fieldId, StringToSomething)
            .parse(args);
        if (arguments.help) System.exit(printCliHelp());

        // ... do your stuff
    } catch (InvalidCommandLine e) {
        printCliHelpWithError(e, Arguments.class);
    }
}
```

You define a class with some fields and annotate them with the Argument annotation.