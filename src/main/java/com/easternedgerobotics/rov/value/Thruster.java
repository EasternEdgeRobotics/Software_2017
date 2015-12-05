package com.easternedgerobotics.rov.value;

class Thruster implements ImmutableValueCompanion<ThrusterValue> {
    public String name;

    public float speed;

    public float voltage;

    public float current;

    public float temperature;

    @Override
    public final ThrusterValue asImmutable() {
        return new ThrusterValue(this);
    }
}
