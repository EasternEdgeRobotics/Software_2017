package com.easternedgerobotics.rov.value;

public class Heartbeat implements ImmutableValueCompanion<HeartbeatValue> {
    public boolean operational;

    @Override
    public final HeartbeatValue asImmutable() {
        return new HeartbeatValue(this);
    }
}
