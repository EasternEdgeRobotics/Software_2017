package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.ThrusterSpeedValue;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Thruster {
    private static final byte WRITE_ADDRESS = 0x00;

    private final byte[] zeroBuffer = new byte[] {0x00, 0x00};

    private I2CDevice device;

    private ThrusterSpeedValue thrusterValue;

    private boolean initialized;

    public Thruster(final EventPublisher event, final ThrusterSpeedValue thruster, final I2CDevice device) {
        this.device = device;
        thrusterValue = thruster;
        initialized = false;

        event.valuesOfType(ThrusterSpeedValue.class)
            .filter(value -> thrusterValue.getName().equals(value.getName()))
            .subscribe(value -> thrusterValue = value);
    }

    /**
     * Write the latest {@code ThrusterSpeedValue} for this thruster to the device.
     * @throws IOException
     */
    public final void write() throws IOException {
        final short speed = (short) (thrusterValue.getSpeed() * Short.MAX_VALUE);
        final byte[] writeBuffer = ByteBuffer.allocate(2).putShort(speed).array();
        if (!initialized) {
            device.write(WRITE_ADDRESS, zeroBuffer, 0, zeroBuffer.length);
            initialized = true;
        }
        device.write(WRITE_ADDRESS, writeBuffer, 0, writeBuffer.length);
    }

    /**
     * Write zero to the thruster.
     * @throws IOException
     */
    public final void writeZero() throws IOException {
        device.write(WRITE_ADDRESS, zeroBuffer, 0, zeroBuffer.length);
    }
}
