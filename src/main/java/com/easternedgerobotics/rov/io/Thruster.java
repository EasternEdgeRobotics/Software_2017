package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.ThrusterSensorValue;
import com.easternedgerobotics.rov.value.ThrusterSpeedValue;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Thruster {

    private static final int BYTE_SIZE = 8;

    private static final byte WRITE_ADDRESS = 0x00;

    private static final byte READ_ADDRESS = 0x02;

    private static final int READ_BUFFER_SIZE = 9;

    private static final int VOLTAGE_INDEX = 2;

    private static final float VOLTAGE_SCALAR = 0.0004921f;

    private static final int CURRENT_INDEX = 6;

    private static final float CURRENT_SCALAR = 0.001122f;

    private static final int CURRENT_OFFSET = 32767;

    private static final int TEMPERATURE_INDEX = 4;

    private static final int SERIESRESISTOR = 3300;

    private static final int THERMISTORNOMINAL = 10000;

    private static final int BCOEFFICIENT = 3900;

    private static final int TEMPERATURENOMINAL = 25;

    private static final float CELSIUSCONVERSION = 273.15f;

    private final byte[] zeroBuffer = new byte[] {0x00, 0x00};

    private I2CDevice device;

    private ThrusterSpeedValue thrusterValue;

    private EventPublisher eventPublisher;

    private boolean initialized;

    public Thruster(final EventPublisher event, final ThrusterSpeedValue thruster, final I2CDevice device) {
        this.device = device;
        eventPublisher = event;
        thrusterValue = thruster;
        initialized = false;

        eventPublisher.valuesOfType(ThrusterSpeedValue.class)
            .filter(value -> thrusterValue.getName().equals(value.getName()))
            .subscribe(value -> thrusterValue = value);
    }

    public final void write() throws IOException {
        final short speed = (short) (thrusterValue.getSpeed() * Short.MAX_VALUE);
        final byte[] writeBuffer = ByteBuffer.allocate(2).putShort(speed).array();
        if (!initialized) {
            device.write(WRITE_ADDRESS, zeroBuffer, 0, zeroBuffer.length);
            initialized = true;
        }
        device.write(WRITE_ADDRESS, writeBuffer, 0, writeBuffer.length);
    }

    public final void writeZero() throws IOException {
        device.write(WRITE_ADDRESS, zeroBuffer, 0, zeroBuffer.length);
    }

    public final void read() throws IOException {
        final byte[] readBuffer = new byte[READ_BUFFER_SIZE];
        device.read(READ_ADDRESS, readBuffer, 0, readBuffer.length);

        final float voltage = parseShort(readBuffer, VOLTAGE_INDEX) * VOLTAGE_SCALAR;
        final float current = (parseShort(readBuffer, CURRENT_INDEX) - CURRENT_OFFSET) * CURRENT_SCALAR;
        final float temperature = calculateTemperature(parseShort(readBuffer, TEMPERATURE_INDEX));

        eventPublisher.emit(ThrusterSensorValue.create(
            thrusterValue.getName(),
            voltage,
            current,
            temperature
        ));
    }

    /**
     * Reads a short from two consecutive bytes.
     *
     * @param bytes an array of two or more bytes, two of which represent a short value
     * @param offset the offset of the sort to read
     * @return the short representation of {@code bytes[offset]} and {@code bytes[offset + 1]}
     */
    private short parseShort(final byte[] bytes, final int offset) {
        return (short) (((bytes[offset]) << BYTE_SIZE) | bytes[offset + 1]);
    }

    /**
     * See <a href="http://docs.bluerobotics.com/bluesc/">BlueESC documentation</a>.
     *
     * @param tempRaw the raw temperature value from the thruster
     * @return the parsed temperature
     */
    private float calculateTemperature(final short tempRaw) {
        float steinhart;
        if (tempRaw == 0) {
            steinhart = 0;
        } else {
            final float resistance = SERIESRESISTOR / (65535 / tempRaw - 1);

            steinhart = resistance / THERMISTORNOMINAL;
            steinhart = (float) Math.log(steinhart);
            steinhart /= BCOEFFICIENT;
            steinhart += 1.0f / (TEMPERATURENOMINAL + CELSIUSCONVERSION);
            steinhart = 1.0f / steinhart;
            steinhart -= CELSIUSCONVERSION;
        }
        return steinhart;
    }
}
