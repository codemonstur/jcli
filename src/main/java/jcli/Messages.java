package jcli;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Looks up help and error text in a user supplied {@link ResourceBundle}.
 *
 * Anything the bundle does not provide falls back to the built in English text, so a bundle can be partial.
 * Values in the bundle are formatted with {@link MessageFormat}, the English defaults are used as they are.
 */
public final class Messages {

    public static final Messages DEFAULT = new Messages(null);

    private final ResourceBundle bundle;

    public Messages(final ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public boolean hasBundle() {
        return bundle != null;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public String get(final String key, final String english) {
        if (bundle == null) return english;
        try {
            return bundle.containsKey(key) ? bundle.getString(key) : english;
        } catch (final MissingResourceException e) {
            return english;
        }
    }

    public String format(final String key, final String english, final Object... params) {
        if (bundle == null || !bundle.containsKey(key)) return english;
        try {
            return MessageFormat.format(bundle.getString(key), params);
        } catch (final MissingResourceException e) {
            return english;
        }
    }

}
