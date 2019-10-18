package jcli.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface CliCommand {

    String name() default "";
    String description() default "";
    String[] examples() default "";

}
