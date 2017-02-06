package com.easternedgerobotics.rov.io.joystick;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import rx.Observable;
import rx.Scheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LogitechExtremeJoystickSource {
    private static final String NAME = ".*Logitech.*Extreme.*3D.*";

    private final Observable<Joystick> joystickSource;

    private final Scheduler scheduler;

    private Controller controller;

    /**
     * Creates a {@code LogitechExtremeJoystickSource} and returns its {@code Joystick} observable.
     *
     * @param sleepDuration The polling interval for recovering a disconnected joystick.
     * @param unit The unit of time for {@code sleepDuration}.
     * @param clock The scheduler for joystick recovery.
     *
     * @return An {@code Observable} that sources {@code Joystick}s.
     */
    public static Observable<Joystick> create(
            final long sleepDuration,
            final TimeUnit unit,
            final Scheduler scheduler) {
        final Observable<Long> interval = Observable.interval(sleepDuration, unit, scheduler);
        final LogitechExtremeJoystickSource logitechSource = new LogitechExtremeJoystickSource(interval, scheduler);
        return logitechSource.getSource();
    }

    private LogitechExtremeJoystickSource(final Observable<Long> interval, final Scheduler scheduler) {
        this.scheduler = scheduler;
        joystickSource = Observable.create(sub -> sub.add(interval.subscribe(tick -> {
            if (controller == null || !controller.poll()) {
                controller = connect();
                if (controller != null) {
                    sub.onNext(new LogitechExtremeJoystick(
                        createEvents(controller),
                        createAxes(controller),
                        createButtons(controller)));
                }
            }
        })));
    }

    private Observable<Joystick> getSource() {
        return joystickSource;
    }

    private Controller connect() {
        final Controller[] controllers = Joysticks.availableControllers();
        for (Controller c : controllers) {
            if (Pattern.matches(NAME, c.getName())) {
                return c;
            }
        }
        return null;
    }

    private Observable<Event> createEvents(final Controller controller) {
        final Observable<Event> events = Observable.create(subscriber -> {
            final EventQueue eventQueue = controller.getEventQueue();
            subscriber.add(Observable.interval(0, TimeUnit.MILLISECONDS, scheduler).subscribe(tick -> {
                if (controller.poll()) {
                    final Event event = new Event();
                    if (eventQueue.getNextEvent(event)) {
                        subscriber.onNext(event);
                    }
                } else {
                    subscriber.unsubscribe();
                }
            }));
        });

        return events.subscribeOn(scheduler).share();
    }

    private static List<Component> createAxes(final Controller controller) {
        return Stream.of(controller.getComponents())
            .filter(component -> component.getIdentifier() instanceof Component.Identifier.Axis)
            .collect(Collectors.toList());
    }

    private static List<Component> createButtons(final Controller controller) {
        return Stream.of(controller.getComponents())
            .filter(component -> component.getIdentifier() instanceof Component.Identifier.Button)
            .collect(Collectors.toList());
    }

}
