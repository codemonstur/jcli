package jcli.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.annotations.Constants.FAKE_NULL;

@Retention(RUNTIME)
public @interface CliOption {

    boolean isHelp() default false;
    boolean isMandatory() default false;
    boolean isVersion() default false;
    char name() default ' ';
    String longName() default "";
    // I would love to set 'default null' here, but this is not allowed for annotations
    String defaultValue() default FAKE_NULL;
    // Useful if you want to configure a default value to show in the help but not actually
    // have it be there for your code
    boolean setDefaultWhenMissing() default true;
    String description() default "";

    // How many values this option consumes:
    //   0  infer from the type: booleans are flags, everything else takes one value
    //   1  exactly one value, makes a Boolean field take an explicit true/false value
    //  >1  exactly that many values, requires a List field
    //  -1  as many values as possible, requires a List field
    int arity() default 0;
    // Count the occurrences of a flag instead of setting it, requires an int/Integer or long/Long field
    boolean isCounting() default false;
    // Read the value from a prompt on the console when it is not on the command line
    boolean isInteractive() default false;

    // Extra names for the same option, a single character is a short name, anything else a long name
    String[] aliases() default {};
    // Keep the option out of the help output
    boolean isHidden() default false;
    // Marks the option deprecated in the help and warns when it is used
    boolean isDeprecated() default false;
    // Section heading in the help output, empty means the default options section
    String group() default "";

    // Value validation
    String[] allowedValues() default {};
    String regex() default "";
    String min() default "";
    String max() default "";

    // Names of other options of this command that must, or must not, be present
    String[] requires() default {};
    String[] excludes() default {};

    // Fall back to these sources when the option is not on the command line
    // Resolution order: system property, environment variable, defaults map, defaultValue
    String systemProperty() default "";
    String environmentVariable() default "";

}
