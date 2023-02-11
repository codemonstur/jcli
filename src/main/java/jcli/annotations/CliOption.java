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

}
