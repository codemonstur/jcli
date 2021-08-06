package jcli.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.annotations.Constants.FAKE_NULL;

@Retention(RUNTIME)
public @interface CliPositional {
    String defaultValue() default FAKE_NULL;
}
