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

    private static final int SERIES_RESISTOR = 3300;

    private static final int THERMISTOR_NOMINAL = 10000;

    private static final int THERMISTOR_BETA_COEFFICIENT = 3900;

    private static final int TEMPERATURE_NOMINAL = 25;

    private static final float CELSIUS_CONVERSION = 273.15f;

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

    public final void read() throws IOException {
        final byte[] readBuffer = new byte[READ_BUFFER_SIZE];
        device.read(READ_ADDRESS, readBuffer, 0, readBuffer.length);

        final float voltage = parse(readBuffer, VOLTAGE_INDEX) * VOLTAGE_SCALAR;
        final float current = (parse(readBuffer, CURRENT_INDEX) - CURRENT_OFFSET) * CURRENT_SCALAR;
        final float temperature = calculateTemperature(parse(readBuffer, TEMPERATURE_INDEX));

        eventPublisher.emit(ThrusterSensorValue.create(
            thrusterValue.getName(),
            voltage,
            current,
            temperature
        ));
    }

    /**
     * Parses an unsigned short value from two consecutive bytes.
     *
     * @param bytes an array of two or more bytes, two of which represent an unsigned short value
     * @param offset the offset of the short to read
     * @return the unsigned short representation of {@code bytes[offset]} and {@code bytes[offset + 1]}
     */
    private int parse(final byte[] bytes, final int offset) {
        return Short.toUnsignedInt(
            ByteBuffer.allocate(2).put(bytes[offset]).put(bytes[offset + 1]).getShort(0));
    }

    /**
     * Calculates temperature with
     * the <a href="https://en.wikipedia.org/wiki/Steinhart–Hart_equation">Steinhart–Hart equation</a>.
     * <p>
     * See also: <a href="http://docs.bluerobotics.com/bluesc/#data-conversion">BlueESC Documentation</a>
     * under "Data Conversion", the subsection about temperature.
     *
     * @param raw the raw ADC measurement scaled to 16 bits from the T200 thruster
     * @return the temperature of the thruster
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private float calculateTemperature(final int raw) {
        double steinhart;
        steinhart = (double) (SERIES_RESISTOR / (65535 / raw - 1)) / THERMISTOR_NOMINAL;
        steinhart = Math.log(steinhart);
        steinhart /= THERMISTOR_BETA_COEFFICIENT;
        steinhart += 1.0 / (TEMPERATURE_NOMINAL + CELSIUS_CONVERSION);
        steinhart = 1.0 / steinhart;
        steinhart -= CELSIUS_CONVERSION;
        return (float) steinhart;
    }
}
