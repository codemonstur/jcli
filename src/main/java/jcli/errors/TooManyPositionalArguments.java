package jcli.errors;

public final class TooManyPositionalArguments extends InvalidCommandLine {
    public TooManyPositionalArguments(final int totalNumberOfPositionalArguments, final int positionalArgumentIndex) {
        super("You have provided too many positional arguments. There are " + totalNumberOfPositionalArguments + " but you have provided " + positionalArgumentIndex);
    }
}
