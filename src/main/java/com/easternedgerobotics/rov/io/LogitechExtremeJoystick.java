package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.MotionValue;

import net.java.games.input.Component;
import net.java.games.input.Event;
import rx.Observable;

import java.util.List;

public class LogitechExtremeJoystick implements Joystick {
    public static final int AXIS_INDEX_HEAVE = 3;

    public static final int AXIS_INDEX_SWAY = 0;

    public static final int AXIS_INDEX_SURGE = 1;

    public static final int AXIS_INDEX_YAW = 2;

    private final Observable<Event> events;

    private final List<Component> axes;

    private final List<Component> buttons;

    public LogitechExtremeJoystick(
        final Observable<Event> events,
        final List<Component> axes,
        final List<Component> buttons
    ) {
        this.events = events.share();
        this.axes = axes;
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

        return Observable.switchOnNext(
            joystickTrigger.map(press ->
                events.filter(this::isAxisEvent).map(e -> createMotionValue(press))));
    }

    private boolean isAxisEvent(final Event event) {
        return event.getComponent().getIdentifier() instanceof Component.Identifier.Axis;
    }

    private MotionValue createMotionValue(final boolean rolling) {
        final float heave = axes.get(AXIS_INDEX_HEAVE).getPollData();
        final float sway = -axes.get(AXIS_INDEX_SWAY).getPollData();
        final float surge = axes.get(AXIS_INDEX_SURGE).getPollData();
        final float yaw = axes.get(AXIS_INDEX_YAW).getPollData();

        if (rolling) {
            return new MotionValue(heave, 0, surge, 0, yaw, -sway);
        }

        return new MotionValue(heave, sway, surge, 0, yaw, 0);
    }
}
