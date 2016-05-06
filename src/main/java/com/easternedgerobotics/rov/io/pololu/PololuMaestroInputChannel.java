package com.easternedgerobotics.rov.io.pololu;

public class PololuMaestroInputChannel extends PololuMaestroChannel {
    /**
     * Constructs an new {@code PololuMaestroInputChannel} instance.
     *
     * @param maestro the Pololu Maestro instance
     * @param channel the channel number
     */
    public PololuMaestroInputChannel(final PololuMaestro maestro, final byte channel) {
        super(maestro, channel);
    }

    /**
     * Returns the voltage of this channel.
     * @return the voltage of this channel
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final float voltage() {
        return 5 * (getPosition() / 1023.0f);
    }
}
