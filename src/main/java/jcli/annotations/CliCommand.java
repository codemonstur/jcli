package jcli.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The CliCommand annotation.
 */
@Retention(RUNTIME)
public @interface CliCommand {

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
    // Shown below the options in the help output
    /**
     * footer.
     * @return the result
     */
    String footer() default "";
    /**
     * examples.
     * @return the result
     */
    String[] examples() default {};

}
