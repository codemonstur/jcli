package jcli;

import jcli.errors.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static jcli.CliParser.FAKE_NULL;
import static jcli.Reflection.*;

public enum CliHelp {;

    public static void printHelp(final String name, final Class clazz) {
        System.out.println(getHelp(name, clazz));
    }

    public static String getHelp(final String name, final Class clazz) throws InvalidOptionConfiguration {
        final StringBuilder builder = new StringBuilder();
        builder.append("usage: ./")
               .append(name)
               .append(" [options]\n\n")
               .append("options:\n");

        for (final Field field : listFields(clazz)) {
            if (!field.isAnnotationPresent(CliOption.class)) continue;
            if (Modifier.isStatic(field.getModifiers())) throw new InvalidModifierStatic(field);
            if (Modifier.isFinal(field.getModifiers())) throw new InvalidModifierFinal(field);

            final CliOption option = field.getAnnotation(CliOption.class);
            if (!isValidFieldType(field.getType()))
                throw new InvalidOptionType(option);
            if (option.name() == ' ' && option.longName().isEmpty())
                throw new InvalidOptionName(field.getName());
            if (option.isFlag() && !isBooleanType(field))
                throw new FlagTypeNotBoolean(field);
            if (!option.isMandatory() && !option.isFlag() && !option.isHelp() && option.defaultValue().equals(FAKE_NULL))
                throw new MissingDefaultForOption(option);

            builder.append("\t")
                .append(toName(option))
                .append("\t")
                .append(option.isMandatory() ? "mandatory" : " optional")
                .append(" ")
                .append(option.isFlag() || option.isHelp() ? " flag" : "value")
                .append("\t\t")
                .append(option.description())
                .append("\n");
        }

        return builder.toString();
    }

    private static String toName(final CliOption option) {
        final List<String > builder = new ArrayList<>();
        if (option.name() != ' ') builder.add("-"+option.name());
        if (!option.longName().isEmpty()) builder.add("--"+option.longName());
        return "(" + String.join(" / ", builder) + ")";
    }
}
