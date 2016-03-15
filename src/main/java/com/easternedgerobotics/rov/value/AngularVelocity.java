package com.easternedgerobotics.rov.value;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

class AngularVelocity implements ImmutableValueCompanion<AngularVelocityValue> {

    public Vector3D velocity;

    @Override
    public final AngularVelocityValue asImmutable() {
        return new AngularVelocityValue(this);
    }
}
