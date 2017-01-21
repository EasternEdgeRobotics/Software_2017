package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;

import rx.exceptions.Exceptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

final class LPS331AP {
    /**
     * The IMUI2C addresses SA0 is connected to power (high).
     */
    static final byte SA0_LOW_ADDRESS = 0b1011100;

    /**
     * The IMUI2C addresses SA0 is grounded (low).
     */
    static final byte SA0_HIGH_ADDRESS = 0b1011101;

    /**
     * Register used to control pressure and temperature update state.
     */
    private static final byte CTRL_REG1 = (byte) 0x20;

    /**
     * A value which enables pressure and temperature updates.
     */
    private static final byte CTRL1_ENABLE = (byte) 0xE0;

    /**
     * The span of registers holding pressure data.
     */
    private static final int PRESSURE_READ_SIZE = 3;

    /**
     * The span of registers holding temperature data.
     */
    private static final int TEMP_READ_SIZE = 2;

    /**
     * The start register for Pressure data.
     */
    private static final byte PRESS_POUT_XL_REH = (byte) 0x28;

    /**
     * Scalar used in calculating pressure from binary value.
     */
    private static final float PRESSURE_SCALAR = 40960f;

    /**
     * The start register for temperature data.
     */
    private static final byte TEMP_OUT_L = (byte) 0x2B;

    /**
     * Offset used in calculating temperature from binary value.
     */
    private static final float TEMP_OFFSET = 42.5f;

    /**
     * Scalar used in calculating temperature from binary value.
     */
    private static final float TEMP_SCALAR = 480f;

    /**
     * An i2c interface connected to the sensor.
     */
    private final IMUI2C device;

    /**
     * Read from an LPS331AP to receive pressure and temperature data.
     *
     * @param device the IMU chip I2C interface.
     * @throws IOException if there is an error while enabling the device.
     */
    LPS331AP(final IMUI2C device) throws IOException {
        this.device = device;
        device.write(CTRL_REG1, CTRL1_ENABLE);
    }

    /**
     * Read pressure from register data.
     *
     * @param tick ignored.
     * @return the current pressure data.
     */
    InternalPressureValue pollPressure(final long tick) {
        try {
            // Read bytes from OUT_X_L to OUT_Z_H inclusive.
            final byte[] pressureBytes = device.read(PRESS_POUT_XL_REH, PRESSURE_READ_SIZE);
            // Array is in reverse order after bulk read.
            final ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.put((byte) 0x00);
            for (int i = PRESSURE_READ_SIZE - 1; i >= 0; i--) {
                byteBuffer.put(pressureBytes[i]);
            }
            // Set buffer to be read and set its index to 0.
            byteBuffer.flip();
            final IntBuffer intBuffer = byteBuffer.asIntBuffer();
            final int rawPressure = intBuffer.get(0);

            return new InternalPressureValue(rawPressure / PRESSURE_SCALAR);

        } catch (final IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Read temperature from register data.
     *
     * @param tick ignored.
     * @return the current temperature data.
     */
    InternalTemperatureValue pollTemperature(final long tick) {
        try {
            // Read bytes from OUT_X_L to OUT_Z_H inclusive.
            final byte[] temperatureBytes = device.read(TEMP_OUT_L, TEMP_READ_SIZE);
            // Array is in reverse order after bulk read.
            final ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            for (int i = TEMP_READ_SIZE - 1; i >= 0; i--) {
                byteBuffer.put(temperatureBytes[i]);
            }
            // Set buffer to be read and set its index to 0.
            byteBuffer.flip();
            final ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
            final int rawTemperature = shortBuffer.get(0);

            return new InternalTemperatureValue(TEMP_OFFSET + rawTemperature / TEMP_SCALAR);

        } catch (final IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
