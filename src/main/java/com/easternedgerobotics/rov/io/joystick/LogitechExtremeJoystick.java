package com.easternedgerobotics.rov.io.joystick;

import net.java.games.input.Component;
import net.java.games.input.Event;
import rx.Observable;
import rx.Scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

class LogitechExtremeJoystick implements Joystick {
    /**
     * Delay required to avoid incorrect axis value events on re-connect.
     */
    static final long INITIAL_INPUT_DELAY = 1000;

    /**
     * Event objects being emitted by the joystick source.
     */
    private final Observable<Event> events;

    /**
     * Scheduler used to skip startup joystick events.
     */
    private final Scheduler scheduler;

    /**
     * Create a Logitech Extreme Joystick object.
     *
     * @param scheduler the scheduler to use during timed operations.
     * @param events events from the device.
     */
    public LogitechExtremeJoystick(
        final Scheduler scheduler,
        final Observable<Event> events
    ) {
        this.scheduler = scheduler;
        this.events = events.share();
    }

    /**
     * Returns the stream of button presses.
     *
     * @return the stream of button presses.
     */
    @Override
    public final Observable<Boolean> button(final String name) {
        final Pattern pattern = Pattern.compile(name);
        final Observable<Event> buttonEvents = events
            .filter(event -> event.getComponent().getIdentifier() instanceof Component.Identifier.Button)
            .filter(event -> pattern.matcher(event.getComponent().getIdentifier().getName()).matches());

        return buttonEvents.map(event -> event.getValue() == 1f);
    }

    @Override
    public final Observable<Boolean> toggleButton(final String name) {
        final AtomicBoolean isToggled = new AtomicBoolean();
        return button(name)
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .map(x -> isToggled.getAndSet(!isToggled.get()));
    }

    /**
     * Returns the Observable stream of joystick motion.
     *
     * @return a stream of motion values.
     */
    @Override
    public final Observable<Float> axis(final String name) {
        final Pattern pattern = Pattern.compile(name);
        final Observable<Event> axesEvents = events
            .filter(event -> event.getComponent().getIdentifier() instanceof Component.Identifier.Axis)
            .filter(event -> pattern.matcher(event.getComponent().getIdentifier().getName()).matches())
            .skip(INITIAL_INPUT_DELAY, TimeUnit.MILLISECONDS, scheduler);

        return axesEvents.map(event -> event.getComponent().getPollData());
    }
}
