package com.easternedgerobotics.rov.io.joystick;

import com.esotericsoftware.minlog.Log;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

final class Joysticks {
    static void load(final Path directory, final String resource) {
        try {
            final ClassLoader loader = Joysticks.class.getClassLoader();
            Files.copy(
                loader.getResourceAsStream(resource),
                directory.resolve(resource.replace(".jnilib", ".dylib")),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        final Iterable<String> natives = Arrays.asList(
            "jinput-dx8_64.dll",
            "jinput-dx8.dll",
            "jinput-raw_64.dll",
            "jinput-raw.dll",
            "jinput-wintab.dll",
            "libjinput-linux64.so",
            "libjinput-linux.so",
            "libjinput-osx.jnilib");

        try {
            final Path tmpdir = Files.createTempDirectory("jinput");

            natives.forEach(lib -> Joysticks.load(tmpdir, lib));
            System.setProperty("net.java.games.input.librarypath", tmpdir.toString());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Joysticks() {

    }

    /**
     * Returns a fresh array of available controllers.
     * Array is made fresh by instantiating a {@code DefaultControllerEnvironment}
     *
     * @return An array containing all available controllers
     */
    static Controller[] availableControllers() {
        try {
            // Device list is never updated unless this hidden object is instantiated
            @SuppressWarnings("unchecked")
            final Constructor<ControllerEnvironment> constructor = (Constructor<ControllerEnvironment>)
                    Class.forName("net.java.games.input.DefaultControllerEnvironment").getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance().getControllers();
        } catch (final InvocationTargetException
                | ClassNotFoundException
                | IllegalAccessException
                | InstantiationException e
        ) {
            Log.error("Unable to scan for joysticks");
            return new Controller[]{};
        }
    }
}
