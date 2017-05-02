package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.PortAftSpeedValue;
import com.easternedgerobotics.rov.value.PortForeSpeedValue;
import com.easternedgerobotics.rov.value.StarboardAftSpeedValue;
import com.easternedgerobotics.rov.value.StarboardForeSpeedValue;
import com.easternedgerobotics.rov.value.VertAftSpeedValue;
import com.easternedgerobotics.rov.value.VertForeSpeedValue;

import java.util.Arrays;
import java.util.Collections;

public class SixThrusterConfig {
    private MotionPowerValue motionPower = new MotionPowerValue();

    private MotionValue motion = new MotionValue();

    private EventPublisher eventPublisher;

    public SixThrusterConfig(final EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        eventPublisher.valuesOfType(MotionValue.class).subscribe(m -> motion = m);
        eventPublisher.valuesOfType(MotionPowerValue.class).subscribe(m -> motionPower = m);
    }

    public final void update() {
        float starboardFore = 0;
        float portFore = 0;
        float starboardAft = 0;
        float portAft = 0;
        float vertFore = 0;
        float vertAft = 0;

        final float surge = motion.getSurge() * motionPower.getSurge() * motionPower.getGlobal();
        final float heave = motion.getHeave() * motionPower.getHeave();
        final float pitch = motion.getPitch() * motionPower.getPitch() * motionPower.getGlobal();
        final float yaw = motion.getYaw() * motionPower.getYaw() * motionPower.getGlobal();
        final float sway = motion.getSway() * motionPower.getSway() * motionPower.getGlobal();

        /**
         * Assumes inputs use standard 3D cartesian coordinates
         *
         *    y
         *    ^
         *    |
         *    |
         *    O - - > x
         *  z
         *
         * Surge is positive forward
         * Sway is positive right
         * Heave is positive up
         * Yaw is positive counterclockwise about z-axis (i.e. left turn)
         * Roll is positive counterclockwise about y-axis (i.e. roll left)
         **/

        // Ratio of T200 (max forward thrust)/(max reverse thrust)
        final float forwardThrustRatio = 1.182f;

        if (!(surge == 0 && sway == 0 && yaw == 0)) {
            starboardFore = -surge + sway - yaw;
            portFore = -surge - sway + yaw;
            starboardAft = surge + sway + yaw;
            portAft = surge - sway - yaw;

            final float maxThrustHorizontal = Collections.max(Arrays.asList(
                Math.abs(starboardFore),
                Math.abs(starboardAft),
                Math.abs(portFore),
                Math.abs(portAft)
            ));

            final float maxInputMagHorizontal = Collections.max(Arrays.asList(
                Math.abs(surge),
                Math.abs(sway),
                Math.abs(yaw)
            ));

            final float horizontalScalar = maxInputMagHorizontal / maxThrustHorizontal;
            starboardFore *= horizontalScalar;
            starboardAft *= horizontalScalar;
            portFore *= horizontalScalar;
            portAft *= horizontalScalar;
        }

        if (!(heave == 0 && pitch == 0)) {
            vertFore = heave + pitch;
            vertAft = heave - pitch;

            final float maxThrustVertical = Collections.max(Arrays.asList(
                Math.abs(vertFore),
                Math.abs(vertAft)
            ));

            final float maxInputMagVertical = Collections.max(Arrays.asList(
                Math.abs(heave),
                Math.abs(pitch))
            );

            final float verticalScalar = maxInputMagVertical / maxThrustVertical;
            vertFore *= verticalScalar;
            vertAft *= verticalScalar;
        }

        // Positive thrust reduction.
        // Corrects for T200's higher forward efficiency
        // If only one vert is spinning positive, divide that thruster output by the thrust ratio
        if ((vertFore > 0 && vertAft < 0) || (vertFore < 0 && vertAft > 0)) {
            if (vertFore > 0) {
                vertFore = vertFore / forwardThrustRatio;
            } else if (vertAft > 0) {
                vertAft = vertAft / forwardThrustRatio;
            }
        }

        eventPublisher.emit(new PortAftSpeedValue(absIfZero(portAft)));
        eventPublisher.emit(new StarboardAftSpeedValue(absIfZero(starboardAft)));
        eventPublisher.emit(new PortForeSpeedValue(absIfZero(portFore)));
        eventPublisher.emit(new StarboardForeSpeedValue(absIfZero(starboardFore)));
        eventPublisher.emit(new VertAftSpeedValue(absIfZero(vertAft)));
        eventPublisher.emit(new VertForeSpeedValue(absIfZero(vertFore)));
    }

    public final void updateZero() {
        eventPublisher.emit(new PortAftSpeedValue(0));
        eventPublisher.emit(new StarboardAftSpeedValue(0));
        eventPublisher.emit(new PortForeSpeedValue(0));
        eventPublisher.emit(new StarboardForeSpeedValue(0));
        eventPublisher.emit(new VertAftSpeedValue(0));
        eventPublisher.emit(new VertForeSpeedValue(0));
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    private float absIfZero(final float value) {
        // noinspection ConstantConditions
        if ((value == 0.0f) || (value == -0.0f)) {
            return 0f;
        }

        return value;
    }
}
