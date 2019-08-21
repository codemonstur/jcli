package jcli;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.Parser.FAKE_NULL;

@Retention(RUNTIME)
public @interface Argument {

    boolean mandatory() default false;
    // FIXME the name should really just be a char
    String name();
    String longName() default "";
    String _default() default FAKE_NULL;
    String description() default "";

}
