package com.easternedgerobotics.rov.value;

class MotionPower implements ImmutableValueCompanion<MotionPowerValue> {
    public float global;

    public float heave;

    public float sway;

    public float surge;

    public float pitch;

    public float yaw;

    public float roll;

    @Override
    public MotionPowerValue asImmutable() {
        return new MotionPowerValue(this);
    }
}
