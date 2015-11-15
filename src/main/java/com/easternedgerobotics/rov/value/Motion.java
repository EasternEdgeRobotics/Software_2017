package com.easternedgerobotics.rov.value;

class Motion implements ImmutableValueCompanion<MotionValue> {
    public float heave;

    public float sway;

    public float surge;

    public float pitch;

    public float yaw;

    public float roll;

    @Override
    public MotionValue asImmutable() {
        return new MotionValue(this);
    }
}
