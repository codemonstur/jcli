package jcli;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.CliParser.FAKE_NULL;

@Retention(RUNTIME)
public @interface CliPositional {
    int index();
    String defaultValue() default FAKE_NULL;
}
