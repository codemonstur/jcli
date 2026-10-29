package jcli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * The default {@link CliPrompter}: reads from the console without echoing when there is one,
 * and falls back to stdin when the program does not have a console attached.
 */
public final class ConsolePrompter implements CliPrompter {

    /** Creates a new console prompter. */
    public ConsolePrompter() {
    }

    private static final BufferedReader STDIN = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    @Override
    /**
     * prompt.
     * @param message the message
     * @return the result
     */
    public String prompt(final String message) {
        final Console console = System.console();
        if (console != null) {
            final char[] value = console.readPassword("%s", message);
            return value == null ? null : new String(value);
        }
        try {
            System.out.print(message);
            System.out.flush();
            return STDIN.readLine();
        } catch (final IOException e) {
            return null;
        }
    }

}
