package jcli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of parsing a command line that can contain subcommands.
 *
 * Holds the parsed arguments of the root command and of every subcommand along the selected path.
  * @param <T> the type parameter
 */
public final class CommandResult<T> {

    private final List<Object> arguments;
    private final List<String> path;

    /**
     * CommandResult.
     * @param root the root
     */
    public CommandResult(final T root) {
        this(Collections.<Object>singletonList(root), Collections.<String>emptyList());
    }

    CommandResult(final List<Object> arguments, final List<String> path) {
        this.arguments = arguments;
        this.path = path;
    }

    /**
     * The parsed arguments of the root command.
      * @return the result
     */
    public T getRoot() {
        @SuppressWarnings("unchecked")
        final T root = (T) arguments.get(0);
        return root;
    }

    /**
     * The deepest selected subcommand, or null when the command line had no subcommand.
      * @return the result
     */
    public String getSubcommand() {
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }

    /**
     * All subcommand names in the order they were given, empty when there were none.
      * @return the result
     */
    public List<String> getPath() {
        return path;
    }

    /**
     * hasSubcommand.
     * @return the result
     */
    public boolean hasSubcommand() {
        return !path.isEmpty();
    }

    /**
     * The arguments of the first command in the chain that were parsed into the given type.
     * Works for nested subcommands: the type decides which level you get.
     * @return the result
     */
    /**
     * get.
     * @param <U> the type parameter
     * @param type the type
     * @return the result
     */
    @SuppressWarnings("unchecked")
    public <U> U get(final Class<U> type) {
        for (final Object argument : arguments) {
            if (type.isInstance(argument)) return (U) argument;
        }
        throw new IllegalStateException("No arguments of type " + type.getName() + " were parsed, the path was " + path);
    }

    /**
     * getArgumentsChain.
     * @return the result
     */
    List<Object> getArgumentsChain() {
        return arguments;
    }

}
