package com.easternedgerobotics.rov.value;

import java.util.Objects;

public class SpeedValue implements MutableValueCompanion<Speed> {
    /**
     * Creates a SpeedValue with the given name.
     *
     * @param name the logical name of the device
     * @return a zero SpeedValue
     */
    public static SpeedValue zero(final String name) {
        return create(name, 0);
    }

    /**
     * Creates a SpeedValue with the given name and speed.
     *
     * @param name the logical name of the device
     * @param speed the speed of the device
     * @return a SpeedValue
     */
    public static SpeedValue create(final String name, final float speed) {
        final Speed device = new Speed();
        device.name = name;
        device.speed = speed;
        return new SpeedValue(device);
    }

    private final Speed device;

    SpeedValue(final Speed device) {
        this.device = device;
    }

    public final SpeedValue setSpeed(
        final float speed
    ) {
        return SpeedValue.create(
            device.name,
            speed
        );
    }

    public final String getName() {
        return device.name;
    }

    public final float getSpeed() {
        return device.speed;
    }

    @Override
    public final Speed asMutable() {
        return device;
    }

    @Override
    public final String toString() {
        return String.format(
            "Speed={%s, %f}",
            device.name,
            device.speed
        );
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SpeedValue that = (SpeedValue) o;
        return Objects.equals(device, that.device);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(device);
    }
}
