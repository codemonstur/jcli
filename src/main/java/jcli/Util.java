package jcli;

import jcli.model.FieldAndPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;

/**
 * The Util enum.
 */
public enum Util {;

    /**
     * isNullOrEmpty.
     * @param value the value
     * @return the result
     */
    public static boolean isNullOrEmpty(final String value) {
        return value == null || value.isEmpty();
    }

    /**
     * padRight.
     * @param s the s
     * @param n the n
     * @return the result
     */
    public static String padRight(final String s, final int n) {
        if (s.length() == n) return s;
        return String.format("%1$-" + n + "s", s);
    }

    /**
     * How a name is shown to users: '-f' for a single character, '--file' for anything else.
      * @param name the name
      * @return the result
     */
    public static String toDisplayName(final String name) {
        return name.length() == 1 ? "-" + name : "--" + name;
    }

    /**
     * toName.
     * @param option the option
     * @return the result
     */
    public static String toName(final jcli.annotations.CliOption option) {
        return option.longName().isEmpty() ? toDisplayName("" + option.name()) : "--" + option.longName();
    }

    /**
     * toName.
     * @param fap the fap
     * @return the result
     */
    public static String toName(final FieldAndPosition fap) {
        return isNullOrEmpty(fap.position.name()) ? fap.field.getName() : fap.position.name();
    }

    /**
     * The default of a List option is a comma separated list, an empty default is an empty list.
      * @param value the value
      * @return the result
     */
    public static List<String> splitListValue(final String value) {
        if (value == null || value.isEmpty()) return Collections.emptyList();
        final String[] values = value.split(",");
        final List<String> result = new ArrayList<>(values.length);
        for (final String element : values) result.add(element.trim());
        return result;
    }

    /**
     * Finds the candidate closest to what the user typed, or null when nothing is close enough.
     * Used to turn 'unknown option' errors into 'did you mean' suggestions.
     * Single characters get no suggestion: every letter is one edit away from every other.
      * @param value the value
      * @param candidates the candidates
     * @return the result
     */
    public static String closestMatch(final String value, final Collection<String> candidates) {
        if (value.length() < 2) return null;
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        final String lower = value.toLowerCase(Locale.ROOT);
        for (final String candidate : candidates) {
            final int distance = levenshtein(lower, candidate.toLowerCase(Locale.ROOT));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        // Typos are short: at most half the length of the typed value, but never less than two characters
        return bestDistance <= Math.max(2, value.length() / 2) ? best : null;
    }

    /**
     * levenshtein.
     * @param a the a
     * @param b the b
     * @return the result
     */
    private static int levenshtein(final String a, final String b) {
        final int[] previous = new int[b.length() + 1];
        final int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) previous[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                final int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            System.arraycopy(current, 0, previous, 0, previous.length);
        }
        return previous[b.length()];
    }

    /**
     * suggestionMessage.
     * @param suggestion the suggestion
     * @return the result
     */
    public static String suggestionMessage(final String suggestion) {
        return format("Did you mean '%s'?", suggestion);
    }

}
