package com.easternedgerobotics.rov.value;

public class HeartbeatValue implements MutableValueCompanion<Heartbeat> {

    /**
     * Creates a HeartbeatValue with the operational value off.
     *
     * @return a zero HeartbeatValue
     */
    public static HeartbeatValue zero() {
        return new HeartbeatValue(new Heartbeat());
    }

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

    HeartbeatValue(final Heartbeat heartbeat){
        this.heartbeat = heartbeat;
    }

    public boolean isOperational(){
        return heartbeat.operational;
    }

    @Override
    public Heartbeat asMutable() {
        return heartbeat;
    }

    @Override
    public String toString() {
        return String.format(
            "Heartbeat={%d}",
            heartbeat.operational
        );
    }
}