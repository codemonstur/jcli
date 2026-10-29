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

    /**
     * The null.
     */
    public static final Messages DEFAULT = new Messages(null);

    private final ResourceBundle bundle;

    /**
     * Messages.
     * @param bundle the bundle
     */
    public Messages(final ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * hasBundle.
     * @return the result
     */
    public boolean hasBundle() {
        return bundle != null;
    }

    /**
     * getBundle.
     * @return the result
     */
    public ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * get.
     * @param key the key
     * @param english the english
     * @return the result
     */
    public String get(final String key, final String english) {
        if (bundle == null) return english;
        try {
            return bundle.containsKey(key) ? bundle.getString(key) : english;
        } catch (final MissingResourceException e) {
            return english;
        }
    }

    /**
     * format.
     * @param key the key
     * @param english the english
     * @param params the params
     * @return the result
     */
    public String format(final String key, final String english, final Object... params) {
        if (bundle == null || !bundle.containsKey(key)) return english;
        try {
            return MessageFormat.format(bundle.getString(key), params);
        } catch (final MissingResourceException e) {
            return english;
        }
    }

}
