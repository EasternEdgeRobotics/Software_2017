package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.pololu.PololuMaestroChannel;
import com.easternedgerobotics.rov.value.ThrusterSpeedValue;

import rx.Observable;

public class Thruster {
    private static final float MAX_FORWARD = 1100f;

    private static final float MAX_REVERSE = 1900f;

    private PololuMaestroChannel device;

    private ThrusterSpeedValue value;

    public Thruster(final Observable<ThrusterSpeedValue> values, final PololuMaestroChannel device) {
        this.device = device;
        values.subscribe(v -> value = v);
    }

    /**
     * Write the latest {@code ThrusterSpeedValue} for this thruster to the device.
     */
    public final void write() {
        device.setTarget(rangeMap(value.getSpeed()));
    }

    /**
     * Write zero to the thruster.
     */
    public final void writeZero() {
        device.setTarget(rangeMap(0));
    }

    /**
     * Returns the thruster speed value mapped to the input microseconds valid for the Basic ESCs. See the
     * <a href="http://docs.bluerobotics.com/besc/#specification-table">Specification Table for the Blue Robotics Basic
     * ESCs</a> for more information.
     * @param speed the thruster speed from -1 to 1
     * @return the microseconds for Pulse Width Signal
     */
    private short rangeMap(final float speed) {
        return (short) rangeMap(speed, -1f, 1f, MAX_FORWARD, MAX_REVERSE);
    }

    /**
     * Returns the given value mapped from Range A to Range B. See also
     * <a href="https://git.io/vwrID">{@code EasternEdge.Common.Utils.ExtensionMethods.NumberRangeMapExtensions}</a>.
     * @param value the value to map
     * @param fromA the start of the range the value is in now
     * @param fromB the end of the range the value is in now
     * @param toA the start of the range the value is being mapped to
     * @param toB the end of the range the value is being mapped to
     * @return the mapped value
     */
    private float rangeMap(final float value, final float fromA, final float fromB, final float toA, final float toB) {
        return toA + (toB - toA) * (value - fromA) / (fromB - fromA);
    }
}
