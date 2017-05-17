package com.easternedgerobotics.rov.io.joystick;

import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import rx.Observable;
import rx.Scheduler;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
     * @param scheduler The scheduler for joystick recovery.
     *
     * @return An {@code Observable} that sources {@code Joystick}s.
     */
    public static Observable<Joystick> create(
            final long sleepDuration,
            final TimeUnit unit,
            final Scheduler scheduler
    ) {
        final Observable<Long> interval = Observable.interval(sleepDuration, unit, scheduler);
        final LogitechExtremeJoystickSource logitechSource = new LogitechExtremeJoystickSource(interval, scheduler);
        return logitechSource.getSource();
    }

    private LogitechExtremeJoystickSource(final Observable<Long> interval, final Scheduler scheduler) {
        this.scheduler = scheduler;
        joystickSource = interval.<Joystick>map(tick -> {
            if (controller == null || !controller.poll()) {
                controller = connect();
                if (controller != null) {
                    return new LogitechExtremeJoystick(
                        scheduler,
                        createEvents(controller));
                }
            }
            return null;
        }).filter(joystick -> joystick != null);
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
        final EventQueue eventQueue = controller.getEventQueue();
        final Observable<Event> events = Observable.interval(0, TimeUnit.MILLISECONDS, scheduler).map(tick -> {
            if (controller.poll()) {
                final Event event = new Event();
                if (eventQueue.getNextEvent(event)) {
                    return event;
                }
            }
            return null;
        });
        return events.subscribeOn(scheduler)
            .takeUntil(event -> !controller.poll())
            .filter(event -> event != null)
            .share();
    }
}
