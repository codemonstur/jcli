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
    public static InvalidOptionConfiguration newVersionTypeNotBoolean(final Field field) {
        return new InvalidOptionConfiguration("isVersion set on field that is not a boolean: " + field.getName());
    }
    public static InvalidOptionConfiguration newVersionAndHelp(final Field field) {
        return new InvalidOptionConfiguration("isHelp and isVersion both set on a field: " + field.getName());
    }
    public static InvalidOptionConfiguration newInvalidModifierStatic(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' is declared static. Annotated fields cannot be static.");
    }
    public static InvalidOptionConfiguration newInvalidOptionName(final String fieldName) {
        return new InvalidOptionConfiguration("Options must set either a name or a long name. Field name is " + fieldName);
    }
    public static InvalidOptionConfiguration newDuplicateOptionName(final String name, final Field field) {
        return new InvalidOptionConfiguration("The option name '" + name
                + "' is used by more than one field: " + field.getName());
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

    public static InvalidOptionConfiguration newInvalidArity(final Field field, final int arity) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares an arity of " + arity
                + " which requires a List field");
    }
    public static InvalidOptionConfiguration newCountingArity(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag and cannot declare an arity");
    }
    public static InvalidOptionConfiguration newCountingMandatory(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag and cannot be mandatory, a count always has a value");
    }
    public static InvalidOptionConfiguration newInvalidCountingType(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag, this requires an int, Integer, long or Long field");
    }
    public static InvalidOptionConfiguration newInvalidCountingDefault(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag with a defaultValue that is not a number");
    }
    public static InvalidOptionConfiguration newInteractiveFlag(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is interactive but is a flag. Interactive options take a value, so set an arity of 1.");
    }
    public static InvalidOptionConfiguration newHelpVersionArity(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a help or version flag and cannot declare an arity");
    }
    public static InvalidOptionConfiguration newInvalidRange(final Field field, final String min, final String max) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares min '" + min
                + "' and/or max '" + max + "' which is not valid for a numeric field");
    }
    public static InvalidOptionConfiguration newInvalidRegex(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares a regex that does not compile");
    }
    public static InvalidOptionConfiguration newInvalidAlias(final Field field, final String alias) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares the empty alias '" + alias + "'");
    }
    public static InvalidOptionConfiguration newUnknownRelatedOption(final String owner, final String related) {
        return new InvalidOptionConfiguration("The field '" + owner + "' refers to '" + related
                + "' in its requires/excludes, but no option with that name exists");
    }
    public static InvalidOptionConfiguration newSubcommandWithPositionals(final String name) {
        return new InvalidOptionConfiguration("The subcommand '" + name
                + "' is registered on a class that also has positional arguments, this is not supported");
    }
    public static InvalidOptionConfiguration newDuplicateSubcommand(final String name) {
        return new InvalidOptionConfiguration("The subcommand '" + name + "' is registered more than once");
    }
    public static InvalidOptionConfiguration newInvalidSubcommandName(final String name) {
        return new InvalidOptionConfiguration("A subcommand must have a name that does not start with '-': " + name);
    }
    public static InvalidOptionConfiguration newMissingSubcommandParser(final String name) {
        return new InvalidOptionConfiguration("The subcommand '" + name + "' is registered without a parser");
    }
    public static InvalidOptionConfiguration newInvalidSubcommandConfiguration() {
        return new InvalidOptionConfiguration("This parser has subcommands registered, use parseWithSubcommands instead of parse");
    }
    public static InvalidOptionConfiguration newMissingNoArgConstructor(final Class<?> clazz) {
        return new InvalidOptionConfiguration("The class '" + clazz.getName()
                + "' has no no-arg constructor, needed when the parser is created with a class instead of a Supplier");
    }

}
