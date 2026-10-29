package jcli.errors;

import jcli.annotations.CliOption;

import java.lang.reflect.Field;

/**
 * The InvalidOptionConfiguration class.
 */
public final class InvalidOptionConfiguration extends RuntimeException {
    /**
     * InvalidOptionConfiguration.
     * @param message the message
     */
    private InvalidOptionConfiguration(final String message) {
        super(message);
    }

    /**
     * newHelpTypeNotBoolean.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newHelpTypeNotBoolean(final Field field) {
        return new InvalidOptionConfiguration("isHelp set on field that is not a boolean: " + field.getName());
    }
    /**
     * newVersionTypeNotBoolean.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newVersionTypeNotBoolean(final Field field) {
        return new InvalidOptionConfiguration("isVersion set on field that is not a boolean: " + field.getName());
    }
    /**
     * newVersionAndHelp.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newVersionAndHelp(final Field field) {
        return new InvalidOptionConfiguration("isHelp and isVersion both set on a field: " + field.getName());
    }
    /**
     * newInvalidModifierStatic.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidModifierStatic(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' is declared static. Annotated fields cannot be static.");
    }
    /**
     * newInvalidOptionName.
     * @param fieldName the fieldName
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidOptionName(final String fieldName) {
        return new InvalidOptionConfiguration("Options must set either a name or a long name. Field name is " + fieldName);
    }
    /**
     * newDuplicateOptionName.
     * @param name the name
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newDuplicateOptionName(final String name, final Field field) {
        return new InvalidOptionConfiguration("The option name '" + name
                + "' is used by more than one field: " + field.getName());
    }
    /**
     * newInvalidOptionType.
     * @param option the option
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidOptionType(final CliOption option) {
        return new InvalidOptionConfiguration("Option '" + (option.name() == ' ' ? option.longName() : option.name()) + "' has an invalid type");
    }
    /**
     * newInvalidOptionType.
     * @param positional the positional
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidOptionType(final Field positional) {
        return new InvalidOptionConfiguration("Positional " + positional.getName() + " has an invalid type");
    }
    /**
     * newMissingDefaultForOption.
     * @param argument the argument
     * @return the result
     */
    public static InvalidOptionConfiguration newMissingDefaultForOption(final CliOption argument) {
        return new InvalidOptionConfiguration("The argument '" + argument.name() + "' is optional but has no default.");
    }

    /**
     * newInvalidArity.
     * @param field the field
     * @param arity the arity
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidArity(final Field field, final int arity) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares an arity of " + arity
                + " which requires a List field");
    }
    /**
     * newCountingArity.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newCountingArity(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag and cannot declare an arity");
    }
    /**
     * newCountingMandatory.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newCountingMandatory(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag and cannot be mandatory, a count always has a value");
    }
    /**
     * newInvalidCountingType.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidCountingType(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag, this requires an int, Integer, long or Long field");
    }
    /**
     * newInvalidCountingDefault.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidCountingDefault(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a counting flag with a defaultValue that is not a number");
    }
    /**
     * newInteractiveFlag.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newInteractiveFlag(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is interactive but is a flag. Interactive options take a value, so set an arity of 1.");
    }
    /**
     * newHelpVersionArity.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newHelpVersionArity(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName()
                + "' is a help or version flag and cannot declare an arity");
    }
    /**
     * newInvalidRange.
     * @param field the field
     * @param min the min
     * @param max the max
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidRange(final Field field, final String min, final String max) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares min '" + min
                + "' and/or max '" + max + "' which is not valid for a numeric field");
    }
    /**
     * newInvalidRegex.
     * @param field the field
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidRegex(final Field field) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares a regex that does not compile");
    }
    /**
     * newInvalidAlias.
     * @param field the field
     * @param alias the alias
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidAlias(final Field field, final String alias) {
        return new InvalidOptionConfiguration("The field '" + field.getName() + "' declares the empty alias '" + alias + "'");
    }
    /**
     * newUnknownRelatedOption.
     * @param owner the owner
     * @param related the related
     * @return the result
     */
    public static InvalidOptionConfiguration newUnknownRelatedOption(final String owner, final String related) {
        return new InvalidOptionConfiguration("The field '" + owner + "' refers to '" + related
                + "' in its requires/excludes, but no option with that name exists");
    }
    /**
     * newSubcommandWithPositionals.
     * @param name the name
     * @return the result
     */
    public static InvalidOptionConfiguration newSubcommandWithPositionals(final String name) {
        return new InvalidOptionConfiguration("The subcommand '" + name
                + "' is registered on a class that also has positional arguments, this is not supported");
    }
    /**
     * newDuplicateSubcommand.
     * @param name the name
     * @return the result
     */
    public static InvalidOptionConfiguration newDuplicateSubcommand(final String name) {
        return new InvalidOptionConfiguration("The subcommand '" + name + "' is registered more than once");
    }
    /**
     * newInvalidSubcommandName.
     * @param name the name
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidSubcommandName(final String name) {
        return new InvalidOptionConfiguration("A subcommand must have a name that does not start with '-': " + name);
    }
    /**
     * newMissingSubcommandParser.
     * @param name the name
     * @return the result
     */
    public static InvalidOptionConfiguration newMissingSubcommandParser(final String name) {
        return new InvalidOptionConfiguration("The subcommand '" + name + "' is registered without a parser");
    }
    /**
     * newInvalidSubcommandConfiguration.
     * @return the result
     */
    public static InvalidOptionConfiguration newInvalidSubcommandConfiguration() {
        return new InvalidOptionConfiguration("This parser has subcommands registered, use parseWithSubcommands instead of parse");
    }
    /**
     * newMissingNoArgConstructor.
     * @param clazz the clazz
     * @return the result
     */
    public static InvalidOptionConfiguration newMissingNoArgConstructor(final Class<?> clazz) {
        return new InvalidOptionConfiguration("The class '" + clazz.getName()
                + "' has no no-arg constructor, needed when the parser is created with a class instead of a Supplier");
    }

}
