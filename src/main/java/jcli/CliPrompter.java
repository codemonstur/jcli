package jcli;

/**
 * Supplies the value of an interactive option, see {@link jcli.annotations.CliOption#isInteractive()}.
 *
 * Returning null means the prompt was cancelled, for example because the input stream ended.
 */
public interface CliPrompter {

    /**
     * prompt.
     * @param message the message
     * @return the result
     */
    String prompt(String message);

}
