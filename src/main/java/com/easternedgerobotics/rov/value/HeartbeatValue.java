package com.easternedgerobotics.rov.value;

public class HeartbeatValue implements MutableValueCompanion<Heartbeat> {
    /**
     * Creates a HeartbeatValue with the given values.
     *
     * @param operational the status of rov operation
     * @return a HeartbeatValue
     */
    public static HeartbeatValue create(
        final boolean operational
    ) {
        final Heartbeat heartbeat = new Heartbeat();
        heartbeat.operational = operational;
        return new HeartbeatValue(heartbeat);
    }

    private final Heartbeat heartbeat;

    HeartbeatValue(final Heartbeat hb) {
        this.heartbeat = hb;
    }

    public final boolean isOperational() {
        return heartbeat.operational;
    }

    @Override
    public final Heartbeat asMutable() {
        return heartbeat;
    }

    @Override
    public final String toString() {
        return String.format(
            "Heartbeat={%b}",
            heartbeat.operational
        );
    }
}
