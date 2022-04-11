package jcli.errors;

import jcli.annotations.CliOption;

import java.lang.reflect.Field;

public final class InvalidOptionConfiguration extends RuntimeException {
    private InvalidOptionConfiguration(final String message) {
        super(message);
    }

    public static InvalidOptionConfiguration newHelpTypeNotBoolean(final Field field) {
        return new InvalidOptionConfiguration("isHelp set on field that is not a boolean: " + field.getName());
    }
    public static InvalidOptionConfiguration newInvalidModifierFinal(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' is declared final. This is not supported. For details you can find a comment in the Parser code.");
    }
    public static InvalidOptionConfiguration newInvalidModifierStatic(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' is declared static. Annotated fields cannot be static.");
    }
    public static InvalidOptionConfiguration newInvalidOptionName(final String fieldName) {
        return new InvalidOptionConfiguration("Options must set either a name or a long name. Field name is " + fieldName);
    }
    public static InvalidOptionConfiguration newInvalidOptionType(final CliOption option) {
        return new InvalidOptionConfiguration("Option '" + (option.name() == ' ' ? option.longName() : option.name()) + "' has an invalid type");
    }
    public static InvalidOptionConfiguration newInvalidOptionType(final Field positional) {
        return new InvalidOptionConfiguration("Positional " + positional.getName() + " has an invalid type");
    }
    public static InvalidOptionConfiguration newMissingDefaultForOption(final CliOption argument) {
        return new InvalidOptionConfiguration("The argument '" + argument.name() + "' is optional but has no default.");
    }

}
