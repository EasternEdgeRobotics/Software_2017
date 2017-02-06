package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.value.MotionValue;

import net.java.games.input.Component;
import net.java.games.input.Event;
import rx.Observable;
import rx.Scheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
     * A list of buttons found on the Logitech Extreme Joystick.
     */
    private final List<Component> buttons;

    /**
     * Scheduler used to skip startup joystick events.
     */
    private final Scheduler scheduler;

    /**
     * Latest value polled for heave.
     */
    private float heave;

    /**
     * Latest value polled for sway.
     */
    private float sway;

    /**
     * Latest value polled for surge.
     */
    private float surge;

    /**
     * Latest value polled for yaw.
     */
    private float yaw;

    /**
     * Create a Logitech Extreme Joystick object.
     *
     * @param scheduler the scheduler to use during timed operations.
     * @param events events from the device.
     * @param buttons all active buttons on the device.
     */
    public LogitechExtremeJoystick(
        final Scheduler scheduler,
        final Observable<Event> events,
        final List<Component> buttons
    ) {
        this.scheduler = scheduler;
        this.events = events.share();
        this.buttons = buttons;
    }

    /**
     * Returns the stream of button presses.
     *
     * @return the stream of button presses.
     */
    @Override
    public final Observable<Boolean> button(final int index) {
        final Observable<Event> buttonEvents = events.filter(
            event -> buttons.indexOf(event.getComponent()) == (index - 1));

        return buttonEvents.map(event -> event.getValue() == 1f);
    }

    /**
     * Returns the Observable stream of joystick motion.
     *
     * @return a stream of motion values.
     */
    @Override
    public final Observable<MotionValue> axes() {
        final Observable<Boolean> joystickTrigger = button(1).startWith(false);
        final Observable<Component> components = events.map(Event::getComponent)
            .filter(c -> c.getIdentifier() instanceof Component.Identifier.Axis)
            .skip(INITIAL_INPUT_DELAY, TimeUnit.MILLISECONDS, scheduler);
        return components.withLatestFrom(joystickTrigger, this::createMotionValue);
    }

    /**
     * Map the current axes values into a motion value.
     *
     * @param component the latest axis component to be updated.
     * @param rolling if the ROV is in a roll operation.
     * @return the latest motion value.
     */
    private MotionValue createMotionValue(final Component component, final boolean rolling) {
        if (component.getIdentifier() == Component.Identifier.Axis.X) {
            sway = -component.getPollData();
        } else if (component.getIdentifier() == Component.Identifier.Axis.Y) {
            surge = component.getPollData();
        } else if (component.getIdentifier() == Component.Identifier.Axis.RZ) {
            yaw = component.getPollData();
        } else if (component.getIdentifier() == Component.Identifier.Axis.SLIDER) {
            heave = component.getPollData();
        }
        if (rolling) {
            return new MotionValue(heave, 0, surge, 0, yaw, -sway);
        }
        return new MotionValue(heave, sway, surge, 0, yaw, 0);
    }
}
