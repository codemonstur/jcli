package jcli.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.annotations.Constants.FAKE_NULL;

/**
 * The CliOption annotation.
 */
@Retention(RUNTIME)
public @interface CliOption {

    /**
     * isHelp.
     * @return the result
     */
    boolean isHelp() default false;
    /**
     * isMandatory.
     * @return the result
     */
    boolean isMandatory() default false;
    /**
     * isVersion.
     * @return the result
     */
    boolean isVersion() default false;
    /**
     * name.
     * @return the result
     */
    char name() default ' ';
    /**
     * longName.
     * @return the result
     */
    String longName() default "";
    // I would love to set 'default null' here, but this is not allowed for annotations
    /**
     * defaultValue.
     * @return the result
     */
    String defaultValue() default FAKE_NULL;
    // Useful if you want to configure a default value to show in the help but not actually
    // have it be there for your code
    /**
     * setDefaultWhenMissing.
     * @return the result
     */
    boolean setDefaultWhenMissing() default true;
    /**
     * description.
     * @return the result
     */
    String description() default "";

    // How many values this option consumes:
    //   0  infer from the type: booleans are flags, everything else takes one value
    //   1  exactly one value, makes a Boolean field take an explicit true/false value
    //  >1  exactly that many values, requires a List field
    //  -1  as many values as possible, requires a List field
    /**
     * arity.
     * @return the result
     */
    int arity() default 0;
    // Count the occurrences of a flag instead of setting it, requires an int/Integer or long/Long field
    /**
     * isCounting.
     * @return the result
     */
    boolean isCounting() default false;
    // Read the value from a prompt on the console when it is not on the command line
    /**
     * isInteractive.
     * @return the result
     */
    boolean isInteractive() default false;

    // Extra names for the same option, a single character is a short name, anything else a long name
    /**
     * aliases.
     * @return the result
     */
    String[] aliases() default {};
    // Keep the option out of the help output
    /**
     * isHidden.
     * @return the result
     */
    boolean isHidden() default false;
    // Marks the option deprecated in the help and warns when it is used
    /**
     * isDeprecated.
     * @return the result
     */
    boolean isDeprecated() default false;
    // Section heading in the help output, empty means the default options section
    /**
     * group.
     * @return the result
     */
    String group() default "";

    // Value validation
    /**
     * allowedValues.
     * @return the result
     */
    String[] allowedValues() default {};
    /**
     * regex.
     * @return the result
     */
    String regex() default "";
    /**
     * min.
     * @return the result
     */
    String min() default "";
    /**
     * max.
     * @return the result
     */
    String max() default "";

    // Names of other options of this command that must, or must not, be present
    /**
     * requires.
     * @return the result
     */
    String[] requires() default {};
    /**
     * excludes.
     * @return the result
     */
    String[] excludes() default {};

    // Fall back to these sources when the option is not on the command line
    // Resolution order: system property, environment variable, defaults map, defaultValue
    /**
     * systemProperty.
     * @return the result
     */
    String systemProperty() default "";
    /**
     * environmentVariable.
     * @return the result
     */
    String environmentVariable() default "";

}
