package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.JoystickAxesValue;

import net.java.games.input.Component;
import net.java.games.input.Event;

import rx.Observable;

import java.util.List;

public class LogitechExtremeJoystick implements Joystick {
    private final Observable<Event> events;

    private final List<Component> axes;

    private final List<Component> buttons;

    public LogitechExtremeJoystick(
        final Observable<Event> events,
        final List<Component> axes,
        final List<Component> buttons
    ) {
        this.events = events;
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
     * Returns the Observable stream of axis values.
     *
     * Each emitted value contains a complete view of the joystick in
     * the following order:
     * <p>
     * <pre><code>(x, y, rx, z, t)</code></pre>
     *
     * @return the stream of axis values.
     */
    @Override
    public final Observable<JoystickAxesValue> axes() {
        final Observable<Event> axisEvents = events.filter(
            event -> event.getComponent().getIdentifier() instanceof Component.Identifier.Axis);

        return axisEvents.map(event -> {
            final float[] values = new float[axes.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = axes.get(i).getPollData();
            }
            return JoystickAxesValue.create(values);
        });
    }
}
