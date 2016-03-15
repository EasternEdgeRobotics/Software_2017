package com.easternedgerobotics.rov.value;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AngularVelocityValue implements MutableValueCompanion<AngularVelocity> {

    /**
     * Creates a AngularVelocityValue with the given values.
     *
     * @param velocity the rov angular velocity
     * @return a AngularVelocityValue
     */
    public static AngularVelocityValue create(
        final Vector3D velocity
    ) {
        final AngularVelocity angularVelocity = new AngularVelocity();
        angularVelocity.velocity = velocity;
        return new AngularVelocityValue(angularVelocity);
    }

    /**
     * Creates a AngularVelocityValue with the given values.
     *
     * @param x component the rov angular velocity
     * @param y component the rov angular velocity
     * @param z component the rov angular velocity
     * @return a AngularVelocityValue
     */
    public static AngularVelocityValue create(
        final double x,
        final double y,
        final double z
    ) {
        return create(new Vector3D(x, y, z));
    }

    private final AngularVelocity angularVelocity;

    AngularVelocityValue(final AngularVelocity angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public final Vector3D getVelocity() {
        return angularVelocity.velocity;
    }

    @Override
    public final AngularVelocity asMutable() {
        return angularVelocity;
    }
}
