package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.ThrusterSpeedValue;

import java.util.Arrays;
import java.util.Collections;

public class SixThrusterConfig {

    private ThrusterSpeedValue portAftThruster;

    private ThrusterSpeedValue starboardAftThruster;

    private ThrusterSpeedValue portForeThruster;

    private ThrusterSpeedValue starboardForeThruster;

    private ThrusterSpeedValue portVertThruster;

    private ThrusterSpeedValue starboardVertThruster;

    private MotionPowerValue motionPower = MotionPowerValue.zero();

    private MotionValue motion = MotionValue.zero();

    private EventPublisher eventPublisher;

    public SixThrusterConfig(
        final EventPublisher eventPublisher,
        final ThrusterSpeedValue portAft,
        final ThrusterSpeedValue starboardAft,
        final ThrusterSpeedValue portFore,
        final ThrusterSpeedValue starboardFore,
        final ThrusterSpeedValue portVert,
        final ThrusterSpeedValue starboardVert
    ) {
        this.eventPublisher = eventPublisher;

        portAftThruster = portAft;
        starboardAftThruster = starboardAft;
        portForeThruster = portFore;
        starboardForeThruster = starboardFore;
        portVertThruster = portVert;
        starboardVertThruster = starboardVert;

        eventPublisher.valuesOfType(MotionValue.class).subscribe(m -> motion = m);

        eventPublisher.valuesOfType(MotionPowerValue.class).subscribe(m -> motionPower = m);
    }

    public final void update() {

        float starboardFore = 0;
        float portFore = 0;
        float starboardAft = 0;
        float portAft = 0;
        float starboardVert = 0;
        float portVert = 0;

        final float surge = motion.getSurge() * motionPower.getSurge() * motionPower.getGlobal();
        final float heave = motion.getHeave() * motionPower.getHeave() * motionPower.getGlobal();
        final float roll = motion.getRoll() * motionPower.getRoll() * motionPower.getGlobal();
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


            final float horizontalScalar = 1 / maxThrustHorizontal;
            starboardFore *= horizontalScalar;
            starboardAft *= horizontalScalar;
            portFore *= horizontalScalar;
            portAft *= horizontalScalar;
        }

        if (!(heave == 0 && roll == 0)) {
            starboardVert = heave + roll;
            portVert = heave - roll;

            final float maxThrustVertical = Collections.max(Arrays.asList(
                Math.abs(starboardVert),
                Math.abs(portVert)
            ));

            final float verticalScalar = 1 / maxThrustVertical;
            starboardVert *= verticalScalar;
            portVert *= verticalScalar;
        }

        // Positive thrust reduction.
        // Corrects for T200's higher forward efficiency
        // If only one vert is spinning positive, divide that thruster output by the thrust ratio
        if ((starboardVert > 0 && portVert < 0) || (starboardVert < 0 && portVert > 0)) {
            if (starboardVert > 0) {
                starboardVert = starboardVert / forwardThrustRatio;
            } else if (portVert > 0) {
                portVert = portVert / forwardThrustRatio;
            }
        }

        // We take the negative value on thrusters with counter-rotating propellers (stbd)
        eventPublisher.emit(portAftThruster.setSpeed(absIfZero(portAft)));
        eventPublisher.emit(starboardAftThruster.setSpeed(absIfZero(-starboardAft)));
        eventPublisher.emit(portForeThruster.setSpeed(absIfZero(portFore)));
        eventPublisher.emit(starboardForeThruster.setSpeed(absIfZero(-starboardFore)));
        eventPublisher.emit(portVertThruster.setSpeed(absIfZero(portVert)));
        eventPublisher.emit(starboardVertThruster.setSpeed(absIfZero(-starboardVert)));
    }

    public final void updateZero() {
        eventPublisher.emit(portAftThruster.setSpeed(0));
        eventPublisher.emit(starboardAftThruster.setSpeed(0));
        eventPublisher.emit(portForeThruster.setSpeed(0));
        eventPublisher.emit(starboardForeThruster.setSpeed(0));
        eventPublisher.emit(portVertThruster.setSpeed(0));
        eventPublisher.emit(starboardVertThruster.setSpeed(0));
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
