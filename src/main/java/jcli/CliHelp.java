package jcli;

import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.*;
import jcli.model.HelpOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static jcli.Reflection.*;
import static jcli.Util.isNullOrEmpty;
import static jcli.Util.padRight;
import static jcli.annotations.Constants.FAKE_NULL;
import static jcli.errors.InvalidOptionConfiguration.*;

public enum CliHelp {;

    public static void printHelp(final Class<?> clazz) {
        System.out.println(getHelp(null, clazz));
    }
    public static void printHelp(final String name, final Class<?> clazz) {
        System.out.println(getHelp(name, clazz));
    }

    public static String getHelp(final Class<?> clazz) throws InvalidOptionConfiguration {
        return getHelp(null, clazz);
    }
    public static String getHelp(final String name, final Class<?> clazz) throws InvalidOptionConfiguration {
        return getHelp(name, clazz, Collections.emptySet());
    }
    public static String getHelp(final String name, final Class<?> clazz
            , final Collection<Class<?>> convertedTypes) throws InvalidOptionConfiguration {
        return getHelp(name, "   ", clazz, convertedTypes);
    }

    public static String getHelp(final String builderName, final String indent, final Class<?> clazz) throws InvalidOptionConfiguration {
        return getHelp(builderName, indent, clazz, Collections.emptySet());
    }

    public static String getHelp(final String builderName, final String indent, final Class<?> clazz
            , final Collection<Class<?>> convertedTypes) throws InvalidOptionConfiguration {
        return getHelp(builderName, indent, clazz, convertedTypes, Collections.emptySet());
    }

    static String getHelp(final String builderName, final String indent, final Class<?> clazz
            , final Collection<Class<?>> convertedTypes, final Collection<String> convertedFields)
            throws InvalidOptionConfiguration {
        return getHelp(builderName, indent, clazz, convertedTypes, convertedFields, Messages.DEFAULT, 0
                , Collections.emptyMap());
    }

    static String getHelp(final String builderName, final String indent, final Class<?> clazz
            , final Collection<Class<?>> convertedTypes, final Messages messages, final int width
            , final Map<String, String> subcommands) throws InvalidOptionConfiguration {
        return getHelp(builderName, indent, clazz, convertedTypes, Collections.emptySet(), messages, width, subcommands);
    }

    static String getHelp(final String builderName, final String indent, final Class<?> clazz
            , final Collection<Class<?>> convertedTypes, final Collection<String> convertedFields
            , final Messages messages, final int width, final Map<String, String> subcommands)
            throws InvalidOptionConfiguration {
        final String fixedIndent = indent == null ? "   " : indent;
        final StringBuilder builder = new StringBuilder();
        final String name = processCliCommandAnnotation(builder, builderName, clazz, messages);
        printUsage(builder, name, clazz, messages);

        final List<HelpOption> options = new ArrayList<>();
        final List<String> groups = new ArrayList<>();

        int maxNameLength = 0;
        int maxNeedLength = 0;
        int maxTypeLength = 0;
        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliOption.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw newInvalidModifierStatic(field);

            final CliOption option = field.getAnnotation(CliOption.class);
            if (option.isHidden()) continue;
            // a field converter accepts any type, only it can convert the value
            if (!convertedFields.contains(field.getName()) && !isValidFieldType(field.getType(), convertedTypes))
                throw newInvalidOptionType(option);
            if (option.name() == ' ' && option.longName().isEmpty())
                throw newInvalidOptionName(field.getName());
//            if (hasMissingDefault(option, field))
//                throw new MissingDefaultForOption(option);

            final String optionName = toName(option);
            final String optionNeed = option.isMandatory()
                ? messages.get("help.mandatory", "mandatory")
                : messages.get("help.optional", "optional");
            final boolean isFlag = option.isCounting() || (isBooleanType(field) && option.arity() != 1);
            final String optionType = isFlag || option.isHelp()
                ? messages.get("help.flag", "flag")
                : messages.get("help.value", "value");

            if (maxNameLength < optionName.length()) maxNameLength = optionName.length();
            if (maxNeedLength < optionNeed.length()) maxNeedLength = optionNeed.length();
            if (maxTypeLength < optionType.length()) maxTypeLength = optionType.length();
            if (!isNullOrEmpty(option.group()) && !groups.contains(option.group())) groups.add(option.group());

            options.add(new HelpOption(optionName, optionNeed, optionType, option.description()
                    , option.defaultValue(), option.group(), option.isDeprecated()));
        }
        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliPositional.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw newInvalidModifierStatic(field);

            final CliPositional positional = field.getAnnotation(CliPositional.class);
            final boolean isOptional = !FAKE_NULL.equals(positional.defaultValue());
            final String positionalNeed = isOptional
                ? messages.get("help.optional", "optional")
                : messages.get("help.mandatory", "mandatory");
            final String positionalName = isNullOrEmpty(positional.name()) ? field.getName() : positional.name();

            if (maxNameLength < positionalName.length()) maxNameLength = positionalName.length();
            if (maxNeedLength < positionalNeed.length()) maxNeedLength = positionalNeed.length();
            if (maxTypeLength < "positional".length()) maxTypeLength = "positional".length();

            options.add(new HelpOption(positionalName, positionalNeed
                    , messages.get("help.positional", "positional"), positional.description(), positional.defaultValue()));
        }

        printOptions(builder, options, "", fixedIndent, maxNameLength, maxNeedLength, maxTypeLength, width, messages);
        for (final String group : groups) {
            builder.append(group).append(":\n");
            printOptions(builder, options, group, fixedIndent, maxNameLength, maxNeedLength, maxTypeLength, width, messages);
        }
        printCommands(builder, subcommands, messages);
        printFooter(builder, clazz);

        return builder.toString();
    }

    private static void printOptions(final StringBuilder builder, final List<HelpOption> options, final String group
            , final String indent, final int maxNameLength, final int maxNeedLength, final int maxTypeLength
            , final int width, final Messages messages) {
        for (final HelpOption option : options) {
            if (!group.equals(option.group)) continue;
            final String prefix = indent
                + padRight(option.name, maxNameLength)
                + indent
                + padRight(option.need, maxNeedLength)
                + " "
                + padRight(option.type, maxTypeLength)
                + indent;
            final String text = describe(option, messages);
            if (width <= 0 || prefix.length() + text.length() <= width) {
                builder.append(prefix).append(text).append("\n");
                continue;
            }
            boolean first = true;
            for (final String line : wrap(text, Math.max(10, width - prefix.length()))) {
                builder.append(first ? prefix : spaces(prefix.length())).append(line).append("\n");
                first = false;
            }
        }
    }

    private static String describe(final HelpOption option, final Messages messages) {
        final StringBuilder text = new StringBuilder();
        if (!isNullOrEmpty(option.description)) text.append(option.description);
        if (option.deprecated) {
            if (text.length() > 0) text.append(" ");
            text.append(messages.get("help.deprecated", "[deprecated]"));
        }
        if (!FAKE_NULL.equals(option.defaultValue) && !isNullOrEmpty(option.defaultValue)) {
            if (text.length() > 0) text.append(" ");
            text.append(messages.format("help.default", "[default: " + option.defaultValue + "]", option.defaultValue));
        }
        return text.toString();
    }

    /** Wraps text on spaces, longer words are left alone. */
    private static List<String> wrap(final String text, final int maxWidth) {
        final List<String> lines = new ArrayList<>();
        final StringBuilder line = new StringBuilder();
        for (final String word : text.split(" ")) {
            if (line.length() == 0) line.append(word);
            else if (line.length() + 1 + word.length() <= maxWidth) line.append(" ").append(word);
            else {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private static String spaces(final int count) {
        final StringBuilder spaces = new StringBuilder(count);
        for (int i = 0; i < count; i++) spaces.append(' ');
        return spaces.toString();
    }

    private static void printCommands(final StringBuilder builder, final Map<String, String> subcommands
            , final Messages messages) {
        if (subcommands.isEmpty()) return;
        int maxLength = 0;
        for (final String command : subcommands.keySet())
            if (maxLength < command.length()) maxLength = command.length();
        builder.append(messages.get("help.commands", "Commands:")).append("\n");
        for (final Map.Entry<String, String> entry : subcommands.entrySet())
            builder.append("   ").append(padRight(entry.getKey(), maxLength)).append("   ")
                .append(entry.getValue()).append("\n");
    }

    private static void printFooter(final StringBuilder builder, final Class<?> clazz) {
        if (!clazz.isAnnotationPresent(CliCommand.class)) return;
        final String footer = clazz.getAnnotation(CliCommand.class).footer();
        if (isNullOrEmpty(footer)) return;
        builder.append("\n").append(footer).append("\n");
    }

    private static String processCliCommandAnnotation(final StringBuilder builder, final String builderName
            , final Class<?> clazz, final Messages messages) {
        String name = builderName;
        if (clazz.isAnnotationPresent(CliCommand.class)) {
            final CliCommand command = clazz.getAnnotation(CliCommand.class);

            name = isNullOrEmpty(builderName) ? command.name() : builderName;

            final String description = command.description();
            if (!isNullOrEmpty(description)) builder.append(description).append("\n\n");

            final String[] examples = command.examples();
            if (hasExamples(examples)) {
                builder.append(messages.get("help.examples", "Examples:")).append("\n");
                for (final String example : examples) {
                    builder.append("   ").append(name).append(" ").append(example).append("\n");
                }
                builder.append("\n");
            }
        }
        return name;
    }

    private static void printUsage(final StringBuilder builder, final String name, final Class<?> clazz
            , final Messages messages) {
        final StringBuilder usage = new StringBuilder();
        usage.append(messages.get("help.usage", "Usage:")).append(" ");
        if (!isNullOrEmpty(name)) usage.append(name).append(" ");
        usage.append("[options]");
        final String positionals = join(" ", toPositionalNames(clazz));
        if (!positionals.isEmpty()) usage.append(" ").append(positionals);

        builder.append(usage).append("\n\n")
            .append(messages.get("help.options", "Options:")).append("\n");
    }

    private static boolean hasExamples(String[] examples) {
        if (examples == null) return false;
        for (final String example : examples) {
            if (!example.isEmpty()) return true;
        }
        return false;
    }

    private static List<String> toPositionalNames(Class<?> clazz) {
        final List<String> names = new ArrayList<>();
        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliPositional.class)) continue;
            final CliPositional positional = field.getAnnotation(CliPositional.class);
            names.add(isNullOrEmpty(positional.name()) ? field.getName() : positional.name());
        }
        return names;
    }

    private static String toName(final CliOption option) {
        final List<String > builder = new ArrayList<>();
        if (option.name() != ' ') builder.add("-"+option.name());
        if (!option.longName().isEmpty()) builder.add("--"+option.longName());
        return join(" ", builder);
    }

}
