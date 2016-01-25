package com.easternedgerobotics.rov.value;

import java.util.Arrays;

public class JoystickAxesValue implements MutableValueCompanion<JoystickAxes> {
    /**
     * Creates a JoystickAxesValue with the given axes values.
     *
     * @param values the set of axis values.
     * @return a JoystickAxesValue with the given axes values.
     */
    public static JoystickAxesValue create(final float[] values) {
        final JoystickAxes ja = new JoystickAxes();
        ja.axes = values;
        return new JoystickAxesValue(ja);
    }

    private final JoystickAxes joystickAxes;

    JoystickAxesValue(final JoystickAxes ja) {
        this.joystickAxes = ja;
    }

    /**
     * Returns the number of axes represented.
     *
     * @return the number of axes represented.
     */
    public final int getAxisCount() {
        return joystickAxes.axes.length;
    }

    /**
     * Returns the value at the given axis index.
     *
     * @param index the axis index.
     * @return the value at the given axis index.
     */
    public final float getAxisValue(final int index) {
        return joystickAxes.axes[index];
    }

    @Override
    public final JoystickAxes asMutable() {
        return joystickAxes;
    }

    @Override
    public final String toString() {
        return Arrays.toString(joystickAxes.axes);
    }
}
