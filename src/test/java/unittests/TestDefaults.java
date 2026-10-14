package unittests;

import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;
import jcli.errors.InvalidCommandLine;
import org.junit.Test;

import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.Properties;

import static jcli.CliParserBuilder.newCliParser;
import static org.junit.Assert.*;

public class TestDefaults {

    public static class Args {
        @CliOption(longName = "config")
        public String configFile;
        @CliOption(longName = "mode", systemProperty = "jcli.test.mode")
        public String mode;
        @CliOption(longName = "level", environmentVariable = "JCLI_TEST_LEVEL")
        public String level;
        @CliPositional
        public String source;
    }

    public static class DefaultsBundle extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][]{{"mode", "from-bundle"}, {"source", "bundle-source"}};
        }
    }

    @Test
    public void mapIsLookedUpByLongName() throws Exception {
        final Map<String, Object> defaults = Map.of("config", "from-map");

        final Args arguments = newCliParser(Args::new).defaultsFrom(defaults)
                .parse(new String[]{"bundle-source"});

        assertEquals("from-map", arguments.configFile);
    }

    @Test
    public void mapIsLookedUpByFieldName() throws Exception {
        final Map<String, Object> defaults = Map.of("configFile", "from-map");

        final Args arguments = newCliParser(Args::new).defaultsFrom(defaults)
                .parse(new String[]{"bundle-source"});

        assertEquals("from-map", arguments.configFile);
    }

    @Test
    public void systemPropertyBeatsTheMap() throws Exception {
        System.setProperty("jcli.test.mode", "from-property");
        try {
            final Map<String, Object> defaults = Map.of("mode", "from-map");

            final Args arguments = newCliParser(Args::new).defaultsFrom(defaults)
                    .parse(new String[]{"bundle-source"});

            assertEquals("from-property", arguments.mode);
        } finally {
            System.clearProperty("jcli.test.mode");
        }
    }

    @Test
    public void missingEnvironmentVariableFallsThrough() throws Exception {
        final Map<String, Object> defaults = Map.of("level", "from-map");

        final Args arguments = newCliParser(Args::new).defaultsFrom(defaults)
                .parse(new String[]{"bundle-source"});

        assertEquals("The environment variable JCLI_TEST_LEVEL does not exist in the test JVM, so the map wins"
                , "from-map", arguments.level);
    }

    @Test
    public void defaultsFromAPropertiesInstance() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("config", "from-properties");

        final Args arguments = newCliParser(Args::new).defaultsFrom(properties)
                .parse(new String[]{"bundle-source"});

        assertEquals("from-properties", arguments.configFile);
    }

    @Test
    public void defaultsFromAResourceBundle() throws Exception {
        final Args arguments = newCliParser(Args::new).defaultsFrom(new DefaultsBundle())
                .parse(new String[]{});

        assertEquals("from-bundle", arguments.mode);
        assertEquals("bundle-source", arguments.source);
    }

    @Test
    public void commandLineBeatsEveryDefaultSource() throws Exception {
        System.setProperty("jcli.test.mode", "from-property");
        try {
            final Map<String, Object> defaults = new HashMap<>();
            defaults.put("mode", "from-map");
            defaults.put("config", "from-map");

            final Args arguments = newCliParser(Args::new).defaultsFrom(defaults)
                    .parse(new String[]{"--mode", "from-cli", "cli-source"});

            assertEquals("from-cli", arguments.mode);
            assertEquals("from-map", arguments.configFile);
            assertEquals("cli-source", arguments.source);
        } finally {
            System.clearProperty("jcli.test.mode");
        }
    }

    public static class MandatoryArgs {
        @CliOption(longName = "mode", systemProperty = "jcli.test.mode", isMandatory = true)
        public String mode;
        @CliOption(longName = "config", isMandatory = true)
        public String config;
    }

    @Test
    public void defaultsSatisfyMandatoryOptions() throws Exception {
        System.setProperty("jcli.test.mode", "from-property");
        try {
            final MandatoryArgs arguments = newCliParser(MandatoryArgs::new)
                    .defaultsFrom(Map.of("config", "from-map"))
                    .parse(new String[]{});

            assertEquals("The system property resolves the mandatory option"
                    , "from-property", arguments.mode);
            assertEquals("The defaults map resolves the mandatory option", "from-map", arguments.config);
        } finally {
            System.clearProperty("jcli.test.mode");
        }
    }

    @Test
    public void anUnresolvedMandatoryOptionStillFails() {
        try {
            newCliParser(MandatoryArgs::new).parse(new String[]{});
            fail("Parsing should have failed on the missing mandatory option");
        } catch (final Exception e) {
            assertTrue("Wrong exception thrown: " + e, e instanceof InvalidCommandLine);
            assertTrue("Unexpected message: " + e.getMessage()
                    , e.getMessage().startsWith("Missing argument"));
        }
    }

}
