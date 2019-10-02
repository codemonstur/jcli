package jcli;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.Parser.FAKE_NULL;

@Retention(RUNTIME)
public @interface CliOption {

    boolean isFlag() default false;
    boolean isMandatory() default false;
    char name() default ' ';
    String longName() default "";
    String defaultValue() default FAKE_NULL;
    String description() default "";

}
