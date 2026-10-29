package jcli;

import jcli.errors.InvalidCommandLine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static jcli.Reflection.StringToType;
import static jcli.Reflection.ToFieldType;

/**
 * Everything a single parse needs to know that is not part of the annotated class itself.
 *
 * Package private: it is an implementation detail shared by the parser, the builder and the help generator.
 */
final class ParseContext {

    final Collection<Class<?>> convertedTypes;
    final Messages messages;
    final CliPrompter prompter;
    final ToFieldType convert;
    final boolean ignoreMissingMandatory;
    final boolean allowPrefixedPositionals;
    final Map<String, String> defaults;
    final Map<String, StringToType<?>> fieldConverters;
    final Consumer<String> deprecations;

    ParseContext(final Collection<Class<?>> convertedTypes, final Messages messages, final CliPrompter prompter
            , final ToFieldType convert, final boolean ignoreMissingMandatory, final boolean allowPrefixedPositionals
            , final Map<String, String> defaults, final Map<String, StringToType<?>> fieldConverters
            , final Consumer<String> deprecations) {
        this.convertedTypes = convertedTypes;
        this.messages = messages;
        this.prompter = prompter;
        this.convert = convert;
        this.ignoreMissingMandatory = ignoreMissingMandatory;
        this.allowPrefixedPositionals = allowPrefixedPositionals;
        this.defaults = defaults;
        this.fieldConverters = fieldConverters;
        this.deprecations = deprecations;
    }

    /**
     * localize.
     * @param error the error
     * @return the result
     */
    InvalidCommandLine localize(final InvalidCommandLine error) {
        return messages.hasBundle() ? error.localize(messages.getBundle()) : error;
    }

    static Map<String, String> toDefaultsMap(final Map<?, ?> map) {
        if (map == null || map.isEmpty()) return Collections.emptyMap();
        final Map<String, String> defaults = new HashMap<>();
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) continue;
            defaults.put(String.valueOf(entry.getKey())
                    , entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        return defaults;
    }

}
