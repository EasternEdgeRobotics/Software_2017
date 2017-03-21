package com.easternedgerobotics.rov.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings({"checkstyle:magicnumber"})
public class ConfigTest {
    @Test
    public void doesLoadConfigFromFile() {
        final File defaultConfigFile;
        final File configFile;
        try {
            defaultConfigFile = File.createTempFile("testDefault", ".yml", new File("./"));
            defaultConfigFile.deleteOnExit();
            final BufferedWriter defaultWriter = new BufferedWriter(new FileWriter(defaultConfigFile));
            defaultWriter.write("test:\n"
                + "  stringParam: \"a string\"\n"
                + "  longParam: 1\n"
                + "  intParam: 2\n"
                + "  shortParam: 3\n"
                + "  byteParam: 4\n"
                + "  boolParam: true\n");
            defaultWriter.close();
            configFile = File.createTempFile("test", ".yml", new File("./"));
            configFile.deleteOnExit();
            final BufferedWriter overrideWriter = new BufferedWriter(new FileWriter(configFile));
            overrideWriter.write("test:\n"
                + "  longParam: 5\n");
            overrideWriter.close();

            final TestConfig testConfig = new Config(defaultConfigFile.getName(), configFile.getName())
                .getConfig("test", TestConfig.class);
            Assert.assertEquals("a string", testConfig.stringParam());
            Assert.assertEquals(5, testConfig.longParam());
            Assert.assertEquals(2, testConfig.intParam());
            Assert.assertEquals(3, testConfig.shortParam());
            Assert.assertEquals(4, testConfig.byteParam());
            Assert.assertEquals(true, testConfig.boolParam());
        } catch (final IOException e) {
            Assert.fail(e.getMessage());
        }
    }

}
