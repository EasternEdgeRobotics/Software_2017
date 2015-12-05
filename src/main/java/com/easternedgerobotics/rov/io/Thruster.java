package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.ThrusterValue;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Thruster {

    private static final int BYTE_SIZE = 8;

    private static final byte WRITE_ADDRESS = 0x00;

    private static final byte READ_ADDRESS = 0x02;

    private static final int READ_BUFFER_SIZE = 9;

    private static final int VOLTAGE_INDEX = 0;

    private static final int CURRENT_INDEX = 2;

    private static final float CURRENT_SCALAR = 5.0f * 6.45f / 65536.0f;

    private static final int TEMPERATURE_INDEX = 6;

    private static final float TEMPERATURE_OFFSET = 32767;

    private static final float TEMPERATURE_SCALAR =  5.0f * 14.706f / 65535.0f;

    private final byte[] zeroBuffer = new byte[] {0x00, 0x00};

    private Device device;

    private ThrusterValue thrusterValue;

    private EventPublisher eventPublisher;

    public Thruster(final EventPublisher event, final ThrusterValue thruster, final Device dev) {

        device = dev;

        eventPublisher = event;

        thrusterValue = thruster;

        eventPublisher.valuesOfType(ThrusterValue.class).subscribe(t -> {
            if (thrusterValue.getName().equals(t.getName())) {
                thrusterValue = t;
            }
        });
    }

    public final void write() throws IOException {
        final short speed = (short) (thrusterValue.getSpeed() * Short.MAX_VALUE);
        final byte[] writeBuffer = ByteBuffer.allocate(2).putShort(speed).array();
        device.write(WRITE_ADDRESS, zeroBuffer);
        device.write(WRITE_ADDRESS, writeBuffer);
    }

    public final void writeZero() throws IOException {
        device.write(WRITE_ADDRESS, zeroBuffer);
    }

    public final void read() throws IOException {
        final byte[] readBuffer = device.read(READ_ADDRESS, READ_BUFFER_SIZE);

        final float voltage = parseShort(readBuffer, VOLTAGE_INDEX);
        final float current = parseShort(readBuffer, CURRENT_INDEX) * CURRENT_SCALAR;
        final float temperature = (parseShort(readBuffer, TEMPERATURE_INDEX) - TEMPERATURE_OFFSET) * TEMPERATURE_SCALAR;

        eventPublisher.emit(ThrusterValue.create(
            thrusterValue.getName(),
            thrusterValue.getSpeed(),
            voltage,
            current,
            temperature
        ));
    }

    // reads a short from two consecutive bytes.
    // MSB located at offset.
    // LSB located at offset + 1.
    private short parseShort(final byte[] bytes, final int offset) {
        return (short) (((bytes[offset]) << BYTE_SIZE) | bytes[offset + 1]);
    }
}
