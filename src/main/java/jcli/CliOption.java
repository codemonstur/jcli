package jcli;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.CliParser.FAKE_NULL;

@Retention(RUNTIME)
public @interface CliOption {

    boolean isHelp() default false;
    boolean isMandatory() default false;
    char name() default ' ';
    String longName() default "";
    // I would love to set 'default null' here, but this is not allowed for annotations
    String defaultValue() default FAKE_NULL;
    String description() default "";

}
