package jcli;

import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static jcli.Reflection.isBooleanType;
import static jcli.Reflection.listFields;
import static jcli.Util.isNullOrEmpty;

/**
 * Generates shell completion scripts from the same annotated classes that the parser reads.
 *
 * <pre>
 *    java -cp app.jar:jcli.jar  # print a script and hook it up like any other completion
 *    jcli_completion=$(...)
 * </pre>
 */
public enum CliCompletion {;

    private static final class OptionSpec {
        final String shortName;
        final String longName;
        final boolean flag;
        final String description;
        OptionSpec(final String shortName, final String longName, final boolean flag, final String description) {
            this.shortName = shortName;
            this.longName = longName;
            this.flag = flag;
            this.description = description;
        }
    }

    private static final class Command {
        String name;
        String description = "";
        final List<OptionSpec> options = new ArrayList<>();
        final List<String> positionals = new ArrayList<>();
        final List<Command> subcommands = new ArrayList<>();
    }

    /**
     * toBash.
     * @param clazz the clazz
     * @return the result
     */
    public static String toBash(final Class<?> clazz) {
        return toBash(toCommandName(clazz), clazz);
    }
    /**
     * toBash.
     * @param commandName the commandName
     * @param clazz the clazz
     * @return the result
     */
    public static String toBash(final String commandName, final Class<?> clazz) {
        return bash(toCommand(commandName, clazz));
    }
    /**
     * toBash.
     * @param builder the builder
     * @return the result
     */
    public static String toBash(final CliParserBuilder<?> builder) {
        return bash(toCommand(builder));
    }

    /**
     * toFish.
     * @param clazz the clazz
     * @return the result
     */
    public static String toFish(final Class<?> clazz) {
        return toFish(toCommandName(clazz), clazz);
    }
    /**
     * toFish.
     * @param commandName the commandName
     * @param clazz the clazz
     * @return the result
     */
    public static String toFish(final String commandName, final Class<?> clazz) {
        return fish(toCommand(commandName, clazz));
    }
    /**
     * toFish.
     * @param builder the builder
     * @return the result
     */
    public static String toFish(final CliParserBuilder<?> builder) {
        return fish(toCommand(builder));
    }

    /**
     * toZsh.
     * @param clazz the clazz
     * @return the result
     */
    public static String toZsh(final Class<?> clazz) {
        return toZsh(toCommandName(clazz), clazz);
    }
    /**
     * toZsh.
     * @param commandName the commandName
     * @param clazz the clazz
     * @return the result
     */
    public static String toZsh(final String commandName, final Class<?> clazz) {
        return zsh(toCommand(commandName, clazz));
    }
    /**
     * toZsh.
     * @param builder the builder
     * @return the result
     */
    public static String toZsh(final CliParserBuilder<?> builder) {
        return zsh(toCommand(builder));
    }

    static String toCommandName(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(CliCommand.class)) {
            final String name = clazz.getAnnotation(CliCommand.class).name();
            if (!isNullOrEmpty(name)) return name;
        }
        return clazz.getSimpleName().toLowerCase(Locale.ROOT);
    }

    /**
     * toCommand.
     * @param name the name
     * @param clazz the clazz
     * @return the result
     */
    private static Command toCommand(final String name, final Class<?> clazz) {
        final Command command = new Command();
        command.name = name;
        if (clazz.isAnnotationPresent(CliCommand.class))
            command.description = clazz.getAnnotation(CliCommand.class).description();

        for (final Field field : listFields(clazz)) {
            if (field.isAnnotationPresent(CliOption.class)) {
                final CliOption option = field.getAnnotation(CliOption.class);
                if (option.isHidden()) continue;
                final boolean flag = option.isCounting() || (isBooleanType(field) && option.arity() != 1);
                command.options.add(new OptionSpec(option.name() == ' ' ? null : "" + option.name()
                        , option.longName().isEmpty() ? null : option.longName(), flag, option.description()));
                for (final String alias : option.aliases()) {
                    if (alias.length() == 1)
                        command.options.add(new OptionSpec(alias, null, flag, option.description()));
                    else
                        command.options.add(new OptionSpec(null, alias, flag, option.description()));
                }
            }
            if (field.isAnnotationPresent(CliPositional.class)) {
                final CliPositional positional = field.getAnnotation(CliPositional.class);
                command.positionals.add(isNullOrEmpty(positional.name()) ? field.getName() : positional.name());
            }
        }
        return command;
    }

    /**
     * toCommand.
     * @param builder the builder
     * @return the result
     */
    private static Command toCommand(final CliParserBuilder<?> builder) {
        final Class<?> clazz = builder.getArgumentsClass();
        final Command command = toCommand(isNullOrEmpty(builder.getName()) ? toCommandName(clazz) : builder.getName()
                , clazz);
        for (final Map.Entry<String, CliParserBuilder.Subcommand> entry : builder.getSubcommands().entrySet()) {
            final Command sub = toCommand(entry.getKey(), entry.getValue().parser.getArgumentsClass());
            if (!isNullOrEmpty(entry.getValue().description)) sub.description = entry.getValue().description;
            command.subcommands.add(sub);
        }
        return command;
    }

    /**
     * bash.
     * @param root the root
     * @return the result
     */
    private static String bash(final Command root) {
        final String function = sanitize(root.name) + "_completions";
        final StringBuilder script = new StringBuilder();
        script.append("# bash completion for ").append(root.name).append("\n");
        script.append(function).append("() {\n");
        script.append("    local cur command\n");
        script.append("    cur=\"${COMP_WORDS[COMP_CWORD]}\"\n");
        if (root.subcommands.isEmpty()) {
            script.append("    ").append(bashCompletions(root)).append("\n");
        } else {
            script.append("    command=\"\"\n");
            script.append("    for (( i=1; i < COMP_CWORD; i++ )); do\n");
            script.append("        if [[ \"${COMP_WORDS[i]}\" != -* && -z \"$command\" ]]; then command=\"${COMP_WORDS[i]}\"; fi\n");
            script.append("    done\n");
            script.append("    case \"$command\" in\n");
            for (final Command sub : root.subcommands) {
                script.append("        ").append(sub.name).append(")\n");
                script.append("            ").append(bashCompletions(sub)).append("\n");
                script.append("            ;;\n");
            }
            script.append("        *)\n");
            script.append("            ").append(bashCompletions(root)).append("\n");
            script.append("            ;;\n");
            script.append("    esac\n");
        }
        script.append("}\n");
        script.append("complete -F ").append(function).append(" ").append(root.name).append("\n");
        return script.toString();
    }

    /**
     * bashCompletions.
     * @param command the command
     * @return the result
     */
    private static String bashCompletions(final Command command) {
        final StringBuilder words = new StringBuilder();
        for (final OptionSpec option : command.options) {
            if (option.shortName != null) words.append(" -").append(option.shortName);
            if (option.longName != null) words.append(" --").append(option.longName);
        }
        for (final Command sub : command.subcommands) words.append(" ").append(sub.name);
        return "if [[ $cur == -* ]]; then COMPREPLY=( $(compgen -W \""
            + words.toString().trim() + "\" -- \"$cur\") ); else COMPREPLY=( $(compgen -f -- \"$cur\") ); fi";
    }

    /**
     * fish.
     * @param root the root
     * @return the result
     */
    private static String fish(final Command root) {
        final StringBuilder script = new StringBuilder();
        appendFish(script, root.name, root, "");
        return script.toString();
    }

    private static void appendFish(final StringBuilder script, final String rootName, final Command command
            , final String condition) {
        for (final OptionSpec option : command.options) {
            script.append("complete -c ").append(rootName);
            if (!condition.isEmpty()) script.append(" -n '").append(condition).append("'");
            if (option.shortName != null) script.append(" -s ").append(option.shortName);
            if (option.longName != null) script.append(" -l ").append(option.longName);
            if (!option.flag) script.append(" -r -F");
            if (!isNullOrEmpty(option.description))
                script.append(" -d '").append(option.description.replace("'", "\\'")).append("'");
            script.append("\n");
        }
        if (command.subcommands.isEmpty()) return;
        for (final Command sub : command.subcommands) {
            script.append("complete -c ").append(rootName);
            if (!condition.isEmpty()) script.append(" -n '").append(condition).append("'");
            script.append(" -a ").append(sub.name);
            if (!isNullOrEmpty(sub.description))
                script.append(" -d '").append(sub.description.replace("'", "\\'")).append("'");
            script.append("\n");
        }
        final StringBuilder children = new StringBuilder("not __fish_seen_subcommand_from");
        for (final Command sub : command.subcommands) children.append(" ").append(sub.name);
        final String childCondition = condition.isEmpty() ? children.toString() : condition + "; and " + children;
        for (final Command sub : command.subcommands)
            appendFish(script, rootName, sub, childCondition);
    }

    /**
     * zsh.
     * @param root the root
     * @return the result
     */
    private static String zsh(final Command root) {
        final String function = sanitize(root.name);
        final StringBuilder script = new StringBuilder();
        script.append("#compdef ").append(root.name).append("\n\n");
        script.append(function).append("() {\n");
        script.append("    local state\n");
        script.append("    _arguments \\\n");
        appendZshArguments(script, root, "        ");
        if (root.subcommands.isEmpty()) {
            if (!root.positionals.isEmpty()) script.append("        '*::positional:_files'\n");
        } else {
            script.append("        '1:command:->commands' \\\n");
            script.append("        '*::args:->args'\n");
            script.append("    case $state in\n");
            script.append("        commands)\n");
            script.append("            compadd ");
            for (int i = 0; i < root.subcommands.size(); i++) {
                if (i > 0) script.append(" ");
                script.append(root.subcommands.get(i).name);
            }
            script.append("\n            ;;\n");
            script.append("        args)\n");
            script.append("            case ${words[1]} in\n");
            for (final Command sub : root.subcommands) {
                script.append("                ").append(sub.name).append(")\n");
                script.append("                    _arguments \\\n");
                appendZshArguments(script, sub, "                        ");
                if (!sub.positionals.isEmpty())
                    script.append("                        '*::positional:_files'\n");
                script.append("                    ;;\n");
            }
            script.append("            esac\n");
            script.append("            ;;\n");
            script.append("    esac\n");
        }
        script.append("}\n\n");
        script.append(function).append(" \"$@\"\n");
        return script.toString();
    }

    /**
     * appendZshArguments.
     * @param script the script
     * @param command the command
     * @param indent the indent
     */
    private static void appendZshArguments(final StringBuilder script, final Command command, final String indent) {
        for (final OptionSpec option : command.options)
            script.append(indent).append(zshArgument(option)).append(" \\\n");
    }

    /**
     * zshArgument.
     * @param option the option
     * @return the result
     */
    private static String zshArgument(final OptionSpec option) {
        final String spec = option.shortName != null ? "-" + option.shortName : "--" + option.longName;
        final String description = zshEscape(option.description);
        if (option.flag) return "'" + spec + "[" + description + "]'";
        final String valueName = option.longName != null ? option.longName : option.shortName;
        return "'" + spec + "[" + description + "]:" + valueName + ":_files'";
    }

    /** _arguments uses brackets and quotes as syntax, so descriptions cannot contain them */
    private static String zshEscape(final String description) {
        return (description == null ? "" : description)
            .replace("[", " ").replace("]", " ").replace("'", "").trim();
    }

    /**
     * sanitize.
     * @param name the name
     * @return the result
     */
    private static String sanitize(final String name) {
        final StringBuilder sanitized = new StringBuilder(name.length());
        for (final char c : name.toCharArray())
            sanitized.append(Character.isLetterOrDigit(c) || c == '_' ? c : '_');
        return sanitized.toString();
    }

}
