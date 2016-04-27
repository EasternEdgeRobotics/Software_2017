package com.easternedgerobotics.rov.io.pololu;

/**
 * A particular channel on a Pololu Maestro device.
 */
public class PololuMaestroChannel {
    /**
     * The Pololu Maestro instance.
     */
    private final PololuMaestro maestro;

    /**
     * The channel number.
     */
    private final byte channel;

    /**
     * Constructs an new {@code PololuMaestroChannel} instance.
     * @param maestro the Pololu Maestro instance
     * @param channel the channel number
     */
    public PololuMaestroChannel(final PololuMaestro maestro, final byte channel) {
        this.maestro = maestro;
        this.channel = channel;
    }

    /**
     * Sets the target for the this channel. This method applies {@link PololuMaestro#setTarget(byte, short)} to the
     * given channel.
     * @param target the target
     */
    public final void setTarget(final short target) {
        maestro.setTarget(channel, target);
    }

    /**
     * Returns the position value of the channel. This method delegates to {@link PololuMaestro#getPosition(byte)}.
     * @return the position value of the channel
     */
    public final short getPosition() {
        return maestro.getPosition(channel);
    }
}
