package jcli;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Generates the reflect-config.json that GraalVM native-image needs to keep the annotated fields reachable.
 *
 * The file is written to META-INF/native-image/jcli/reflect-config.json of the compiled output, which
 * native-image picks up automatically when it is on the classpath. Enums used as a field type are listed
 * with allDeclaredFields because the parser looks them up with Enum.valueOf.
 */
@SupportedAnnotationTypes({"jcli.annotations.CliOption", "jcli.annotations.CliPositional"})
public final class CliReflectionProcessor extends AbstractProcessor {

    private final SortedMap<String, SortedSet<String>> classes = new TreeMap<>();
    private final SortedSet<String> enums = new TreeSet<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(CliOption.class)) collect(element);
        for (final Element element : roundEnv.getElementsAnnotatedWith(CliPositional.class)) collect(element);
        if (roundEnv.processingOver()) write();
        return true;
    }

    private void collect(final Element element) {
        if (element.getKind() != ElementKind.FIELD) return;
        final Element enclosing = element.getEnclosingElement();
        if (enclosing == null || enclosing.getKind() != ElementKind.CLASS) return;

        final String className = ((TypeElement) enclosing).getQualifiedName().toString();
        classes.computeIfAbsent(className, key -> new TreeSet<>()).add(element.getSimpleName().toString());

        final TypeMirror type = ((VariableElement) element).asType();
        if (type.getKind() == TypeKind.DECLARED) {
            final Element typeElement = ((DeclaredType) type).asElement();
            if (typeElement.getKind() == ElementKind.ENUM) enums.add(typeElement.toString());
        }
    }

    private void write() {
        if (classes.isEmpty()) return;
        try {
            final FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, ""
                    , "META-INF/native-image/jcli/reflect-config.json");
            try (final Writer writer = file.openWriter()) {
                writer.write(toJson());
            }
        } catch (final IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR
                    , "Failed to write reflect-config.json: " + e.getMessage());
        }
    }

    private String toJson() {
        final StringBuilder json = new StringBuilder("[\n");
        boolean first = true;
        for (final Map.Entry<String, SortedSet<String>> entry : classes.entrySet()) {
            if (!first) json.append(",\n");
            first = false;
            json.append("  {\"name\":\"").append(entry.getKey()).append("\",\"fields\":[");
            boolean firstField = true;
            for (final String field : entry.getValue()) {
                if (!firstField) json.append(",");
                firstField = false;
                json.append("{\"name\":\"").append(field).append("\"}");
            }
            json.append("]}");
        }
        for (final String enumType : enums) {
            if (!first) json.append(",\n");
            first = false;
            json.append("  {\"name\":\"").append(enumType).append("\",\"allDeclaredFields\":true}");
        }
        json.append("\n]\n");
        return json.toString();
    }

}
