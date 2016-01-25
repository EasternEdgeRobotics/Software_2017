package com.easternedgerobotics.rov.io;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Joysticks {
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

    public static Observable<Event> events(final Controller controller) {
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

    public static List<Component> axes(final Controller controller) {
        return Stream.of(controller.getComponents())
            .filter(component -> component.getIdentifier() instanceof Component.Identifier.Axis)
            .collect(Collectors.toList());
    }

    public static List<Component> buttons(final Controller controller) {
        return Stream.of(controller.getComponents())
            .filter(component -> component.getIdentifier() instanceof Component.Identifier.Button)
            .collect(Collectors.toList());
    }

    public static Observable<Controller> availableControllers() {
        final Observable<Controller> controllers = Observable.create(subscriber -> {
            Stream.of(ControllerEnvironment.getDefaultEnvironment().getControllers()).forEach(subscriber::onNext);
            subscriber.onCompleted();
        });

        return controllers;
    }
}
