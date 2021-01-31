package jcli;

import jcli.annotations.CliOption;
import jcli.errors.InvalidCommandLine;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static jcli.CliHelp.getHelp;
import static jcli.CliParser.parseCommandLineArguments;
import static jcli.Reflection.*;

public class CliParserBuilder<T> {

    public interface CliErrorConsumer {
        void cliArgumentsInvalid(InvalidCommandLine e, String[] args);
    }
    public interface CliHelpConsumer<A> {
        void cliHelpRequested(A arguments, String[] args);
    }

    public static <U> CliParserBuilder<U> newCliParser(final Supplier<U> supplier) {
        return new CliParserBuilder<U>().object(supplier);
    }
    public static <U> CliParserBuilder<U> newCliParser() {
        return new CliParserBuilder<>();
    }

    private String name;
    private Supplier<T> supplier;
    private boolean onErrorPrintHelpAndExit = false;
    private boolean onHelpPrintHelpAndExit = false;
    private int helpExitCode = 1;
    private int errorExitCode = 2;
    private CliErrorConsumer onErrorCall;
    private CliHelpConsumer<T> onHelpCall;
    private Consumer<String> helpConsumer = System.out::println;
    private String helpIndent;
    private Map<Class<?>, StringToType<?>> classConverters = new HashMap<>();

    public CliParserBuilder<T> name(final String name) {
        this.name = name;
        return this;
    }
    public CliParserBuilder<T> object(final Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }
    public CliParserBuilder<T> onErrorPrintHelpAndExit() {
        onErrorPrintHelpAndExit = true;
        return this;
    }
    public CliParserBuilder<T> onErrorCall(final CliErrorConsumer consumer) {
        onErrorCall = consumer;
        return this;
    }
    public CliParserBuilder<T> errorExitCode(final int code) {
        this.errorExitCode = code;
        return this;
    }
    public CliParserBuilder<T> onHelpPrintHelpAndExit() {
        onHelpPrintHelpAndExit = true;
        return this;
    }
    public CliParserBuilder<T> onHelpCall(final CliHelpConsumer<T> consumer) {
        onHelpCall = consumer;
        return this;
    }
    public CliParserBuilder<T> helpExitCode(final int code) {
        this.helpExitCode = code;
        return this;
    }
    public CliParserBuilder<T> helpConsumer(final Consumer<String> helpConsumer) {
        this.helpConsumer = helpConsumer;
        return this;
    }
    public CliParserBuilder<T> indent(final String indent) {
        this.helpIndent = indent;
        return this;
    }

    public <C> CliParserBuilder addClassConverter(final Class<C> clazz, final StringToType<C> converter) {
        this.classConverters.put(clazz, converter);
        return this;
    }
// TODO implement field converters
//
//    public <D> CliParserBuilder addFieldConverter(final int fieldId, StringToType<D> converter) {
//
//    }

    // Not pretty but so far the only solution to the checked Exception problem
    public T parseSuppressErrors(final String[] args) {
        try {
            return parse(args);
        } catch (Exception e) {
            return null;
        }
    }

    public T parse(final String[] args) throws InvalidCommandLine {
        final T object = supplier.get();
        try {
            final T arguments = parseCommandLineArguments(args, object, newFieldToType(classConverters));
            if (isHelpSelected(arguments)) {
                if (onHelpCall != null) onHelpCall.cliHelpRequested(arguments, args);
                if (onHelpPrintHelpAndExit) {
                    helpConsumer.accept(getHelp(name, object.getClass()));
                    System.exit(helpExitCode);
                }
            }
            return arguments;
        } catch (InvalidCommandLine e) {
            if (onErrorCall != null) onErrorCall.cliArgumentsInvalid(e, args);
            if (onErrorPrintHelpAndExit) {
                helpConsumer.accept(getHelp(name, object.getClass()));
                System.exit(errorExitCode);
            }
            throw e;
        }
    }

    private static ToFieldType newFieldToType(final Map<Class<?>, StringToType<?>> classConverters) {
        return (type, value) -> {
            for (final Map.Entry<Class<?>, StringToType<?>> entry : classConverters.entrySet()) {
                if (type.isAssignableFrom(entry.getKey())) entry.getValue().toType(value);
            }
            return toFieldType(type, value);
        };
    }

    private boolean isHelpSelected(final T arguments) {
        try {
            for (final Field field : listFields(arguments.getClass())) {
                if (!field.isAnnotationPresent(CliOption.class)) continue;
                makeAccessible(field);
                final CliOption option = field.getAnnotation(CliOption.class);
                if (option.isHelp() && field.get(arguments).equals(TRUE)) {
                    return true;
                }
            }
        } catch (IllegalAccessException e) { /* can't happen so lets just say false */ }

        return false;
    }

}
