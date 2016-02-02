package com.easternedgerobotics.rov.io;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Joysticks {
    static void load(final Path directory, final String resource) {
        try {
            final ClassLoader loader = Joysticks.class.getClassLoader();
            Files.copy(
                loader.getResourceAsStream(resource),
                directory.resolve(resource),
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

    public static Observable<Joystick> logitechExtreme3dPro() {
        final String name = "Logitech Logitech Extreme 3D";
        final Observable<Controller> joystick = availableControllers()
            .first(controller -> controller.getName().equals(name));

        return joystick.map(js -> new LogitechExtremeJoystick(
            events(js),
            axes(js),
            buttons(js)
        ));
    }

    private static Observable<Event> events(final Controller controller) {
        final Observable<Event> events = Observable.create(subscriber -> {
            final EventQueue eventQueue = controller.getEventQueue();
            while (!subscriber.isUnsubscribed()) {
                if (!controller.poll()) {
                    subscriber.onError(new JoystickDisconnectedException());
                    return;
                }

                final Event event = new Event();
                if (!eventQueue.getNextEvent(event)) {
                    continue;
                }
                subscriber.onNext(event);
            }
        });

        return events.subscribeOn(Schedulers.io()).share();
    }

    private static List<Component> axes(final Controller controller) {
        return Stream.of(controller.getComponents())
            .filter(component -> component.getIdentifier() instanceof Component.Identifier.Axis)
            .collect(Collectors.toList());
    }

    private static List<Component> buttons(final Controller controller) {
        return Stream.of(controller.getComponents())
            .filter(component -> component.getIdentifier() instanceof Component.Identifier.Button)
            .collect(Collectors.toList());
    }

    private static Observable<Controller> availableControllers() {
        return Observable.defer(() -> Observable.from(ControllerEnvironment.getDefaultEnvironment().getControllers()));
    }
}
