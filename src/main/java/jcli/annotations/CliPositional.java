package jcli.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jcli.annotations.Constants.FAKE_NULL;

/**
 * The CliPositional annotation.
 */
@Retention(RUNTIME)
public @interface CliPositional {

    /**
     * defaultValue.
     * @return the result
     */
    String defaultValue() default FAKE_NULL;
    // Shown in the help output, defaults to the field name
    /**
     * name.
     * @return the result
     */
    String name() default "";
    /**
     * description.
     * @return the result
     */
    String description() default "";

}
