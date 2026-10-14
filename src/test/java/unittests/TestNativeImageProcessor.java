package unittests;

import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

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
        assertNotNull("The tests must run on a JDK, not a JRE", compiler);

        final int result = compiler.run(null, null, System.err
                , "-proc:only"
                , "-processor", "jcli.CliReflectionProcessor"
                , "-classpath", classpathOfTheLibrary()
                , "-d", output.toString()
                , sourceFile.toString());
        assertEquals("The compilation should succeed", 0, result);

        final Path config = output.resolve("META-INF/native-image/jcli/reflect-config.json");
        assertTrue("reflect-config.json should be written to " + config, Files.exists(config));

        final String json = new String(Files.readAllBytes(config), StandardCharsets.UTF_8);
        assertTrue(json, json.contains("\"name\":\"SampleArgs\""));
        assertTrue(json, json.contains("{\"name\":\"file\"}"));
        assertTrue(json, json.contains("{\"name\":\"mode\"}"));
        assertTrue("Enums are reachable through Enum.valueOf:\n" + json, json.contains("\"SampleArgs.Mode\""));
        assertTrue(json, json.contains("allDeclaredFields"));
    }

    private static String classpathOfTheLibrary() throws Exception {
        final URL location = jcli.CliReflectionProcessor.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(location.toURI()).getAbsolutePath();
    }

}
