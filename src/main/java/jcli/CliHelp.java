package jcli;

import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.*;
import jcli.model.HelpOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;
import static jcli.Reflection.*;
import static jcli.Util.isNullOrEmpty;
import static jcli.Util.padRight;
import static jcli.annotations.Constants.FAKE_NULL;

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
        return getHelp(name, "   ", clazz);
    }

    public static String getHelp(final String builderName, final String indent, final Class<?> clazz) throws InvalidOptionConfiguration {
        final StringBuilder builder = new StringBuilder();
        final String name = processCliCommandAnnotation(builder, builderName, clazz);
        printUsage(builder, name, clazz);

        final List<HelpOption> options = new ArrayList<>();

        int maxNameLength = 0;
        int maxNeedLength = 0;
        int maxTypeLength = 0;
        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliOption.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw new InvalidModifierStatic(field);
            if (Modifier.isFinal(field.getModifiers())) throw new InvalidModifierFinal(field);

            final CliOption option = field.getAnnotation(CliOption.class);
            if (!isValidFieldType(field.getType()))
                throw new InvalidOptionType(option);
            if (option.name() == ' ' && option.longName().isEmpty())
                throw new InvalidOptionName(field.getName());
//            if (hasMissingDefault(option, field))
//                throw new MissingDefaultForOption(option);

            final String optionName = toName(option);
            final String optionNeed = option.isMandatory() ? "mandatory" : "optional";
            final String optionType = isBooleanType(field) || option.isHelp() ? "flag" : "value";

            if (maxNameLength < optionName.length()) maxNameLength = optionName.length();
            if (maxNeedLength < optionNeed.length()) maxNeedLength = optionNeed.length();
            if (maxTypeLength < optionType.length()) maxTypeLength = optionType.length();

            options.add(new HelpOption(optionName, optionNeed, optionType, option.description(), option.defaultValue()));
        }
        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliPositional.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw new InvalidModifierStatic(field);
            if (Modifier.isFinal(field.getModifiers())) throw new InvalidModifierFinal(field);

            final CliPositional positional = field.getAnnotation(CliPositional.class);
            if (FAKE_NULL.equals(positional.defaultValue())) continue;

            if (maxTypeLength < "positional".length()) maxTypeLength = "positional".length();

            options.add(new HelpOption(field.getName(), "optional", "positional", "", positional.defaultValue()));
        }

        for (final HelpOption option : options) {
            builder.append(indent)
                .append(padRight(option.name, maxNameLength))
                .append(indent)
                .append(padRight(option.need, maxNeedLength))
                .append(" ")
                .append(padRight(option.type, maxTypeLength))
                .append(indent)
                .append(option.description);
            if (!FAKE_NULL.equals(option.defaultValue) && !isNullOrEmpty(option.defaultValue)) {
                if (!isNullOrEmpty(option.description)) builder.append(" ");
                builder.append("[default: ").append(option.defaultValue).append("]");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    private static String processCliCommandAnnotation(final StringBuilder builder, final String builderName, final Class<?> clazz) {
        String name = builderName;
        if (clazz.isAnnotationPresent(CliCommand.class)) {
            final CliCommand command = clazz.getAnnotation(CliCommand.class);

            name = isNullOrEmpty(builderName) ? command.name() : builderName;

            final String description = command.description();
            if (!isNullOrEmpty(description)) builder.append(description).append("\n\n");

            final String[] examples = command.examples();
            if (hasExamples(examples)) {
                builder.append("Examples:\n");
                for (final String example : examples) {
                    builder.append("   ").append(name).append(" ").append(example).append("\n");
                }
                builder.append("\n");
            }
        }
        return name;
    }

    private static void printUsage(final StringBuilder builder, final String name, final Class<?> clazz) {
        builder.append("Usage: ")
            .append(name)
            .append(" [options] ")
            .append(join(" ", toPositionalNames(clazz)))
            .append("\n\n")
            .append("Options:\n");
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
            names.add(field.getName());
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
