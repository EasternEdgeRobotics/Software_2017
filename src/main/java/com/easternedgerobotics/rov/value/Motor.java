package com.easternedgerobotics.rov.value;

class Motor implements ImmutableValueCompanion<MotorValue> {
    public String name;

    public float power;

    @Override
    public MotorValue asImmutable() {
        return new MotorValue(this);
    }
}
