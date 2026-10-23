package unittests;

import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestNativeImageProcessor {

    @Test
    public void writesReflectConfigForAnnotatedFields() throws Exception {
        final Path sources = Files.createTempDirectory("jcli-processor-sources");
        final Path output = Files.createTempDirectory("jcli-processor-output");
        final Path sourceFile = sources.resolve("SampleArgs.java");
        Files.write(sourceFile, (
                "import jcli.annotations.CliOption;\n" +
                "public class SampleArgs {\n" +
                "    @CliOption(longName = \"file\")\n" +
                "    public String file;\n" +
                "    @CliOption(longName = \"mode\")\n" +
                "    public Mode mode;\n" +
                "    public enum Mode { READ, WRITE }\n" +
                "}\n").getBytes(StandardCharsets.UTF_8));

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "The tests must run on a JDK, not a JRE");

        final int result = compiler.run(null, null, System.err
                , "-proc:only"
                , "-processor", "jcli.CliReflectionProcessor"
                , "-classpath", classpathOfTheLibrary()
                , "-d", output.toString()
                , sourceFile.toString());
        assertEquals(0, result, "The compilation should succeed");

        final Path config = output.resolve("META-INF/native-image/jcli/reflect-config.json");
        assertTrue(Files.exists(config), "reflect-config.json should be written to " + config);

        final String json = new String(Files.readAllBytes(config), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"name\":\"SampleArgs\""), json);
        assertTrue(json.contains("{\"name\":\"file\"}"), json);
        assertTrue(json.contains("{\"name\":\"mode\"}"), json);
        assertTrue(json.contains("\"SampleArgs.Mode\""), "Enums are reachable through Enum.valueOf:\n" + json);
        assertTrue(json.contains("allDeclaredFields"), json);
        assertTrue(json.contains("allDeclaredConstructors"), "Constructors are reachable for class mode parsing:\n" + json);
    }

    @Test
    public void writesReflectConfigForRecords() throws Exception {
        final Path sources = Files.createTempDirectory("jcli-processor-record-sources");
        final Path output = Files.createTempDirectory("jcli-processor-record-output");
        final Path sourceFile = sources.resolve("RecordArgs.java");
        Files.write(sourceFile, (
                "import jcli.annotations.CliOption;\n" +
                "public record RecordArgs(@CliOption(longName = \"file\") String file) {}\n").getBytes(StandardCharsets.UTF_8));

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "The tests must run on a JDK, not a JRE");

        final int result = compiler.run(null, null, System.err
                , "-proc:only"
                , "-processor", "jcli.CliReflectionProcessor"
                , "-classpath", classpathOfTheLibrary()
                , "-d", output.toString()
                , sourceFile.toString());
        assertEquals(0, result, "The compilation should succeed");

        final Path config = output.resolve("META-INF/native-image/jcli/reflect-config.json");
        assertTrue(Files.exists(config), "reflect-config.json should be written to " + config);

        final String json = new String(Files.readAllBytes(config), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"name\":\"RecordArgs\""), json);
        assertTrue(json.contains("{\"name\":\"file\"}"), json);
        assertTrue(json.contains("allDeclaredConstructors"), "The canonical constructor is reachable for records:\n" + json);
    }

    private static String classpathOfTheLibrary() throws Exception {
        final URL location = jcli.CliReflectionProcessor.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(location.toURI()).getAbsolutePath();
    }

}
