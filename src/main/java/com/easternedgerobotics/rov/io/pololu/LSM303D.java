package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.devices.I2C;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.RotationValue;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

final class LSM303D {
    /**
     * Register used to control acceleration update state.
     */
    static final byte CTRL1 = (byte) 0x20;

    /**
     * Value for CTRL1.
     * 0x57 = 0b01010111
     * AODR = 0101 (50 Hz ODR); AZEN = AYEN = AXEN = 1 (all axes enabled)
     */
    static final byte CTRL1_VAL = (byte) 0x57;

    /**
     * Register used to control  anti-alias filter bandwidth.
     */
    static final byte CTRL2 = (byte) 0x21;

    /**
     * Value for CTRL2.
     * 0x00 = 0b00000000
     * AFS = 0 (+/- 2 g full scale)
     */
    static final byte CTRL2_VAL = (byte) 0x00;

    /**
     * Register used to control magnetic resolution selection.
     */
    static final byte CTRL5 = (byte) 0x24;

    /**
     * Value for CTRL5.
     * 0x64 = 0b01100100
     * M_RES = 11 (high resolution mode); M_ODR = 001 (6.25 Hz ODR)
     */
    static final byte CTRL5_VAL = (byte) 0x64;

    /**
     * Register used to control magnetic full-scale selection.
     */
    static final byte CTRL6 = (byte) 0x25;

    /**
     * Value for CTRL6.
     * 0x20 = 0b00100000
     * MFS = 01 (+/- 4 gauss full scale)
     */
    static final byte CTRL6_VAL = (byte) 0x20;

    /**
     * Register used to control magnetic sensor mode selection.
     */
    static final byte CTRL7 = (byte) 0x26;

    /**
     * Value for CTRL7.
     * 0x00 = 0b00000000
     * MLP = 0 (low power mode off); MD = 00 (continuous-conversion mode)
     */
    static final byte CTRL7_VAL = (byte) 0x00;

    /**
     * The span of registers holding data.
     */
    static final int READ_SIZE = 6;

    /**
     * The start register for acceleration data.
     */
    static final byte OUT_X_L_A = (byte) 0x28;

    /**
     * The start register for magnetometer data.
     */
    static final byte OUT_X_L_M = (byte) 0x08;

    /**
     * An i2c interface connected to the sensor.
     */
    private final I2C device;

    /**
     * Read from an LSM303D to receive acceleration and magnetometer data.
     *
     * @param device the IMU chip I2C interface.
     */
    LSM303D(final I2C device) {
        this.device = device;
        // Accelerometer
        device.write(CTRL2, CTRL2_VAL);
        device.write(CTRL1, CTRL1_VAL);
        // Magnetometer
        device.write(CTRL5, CTRL5_VAL);
        device.write(CTRL6, CTRL6_VAL);
        device.write(CTRL7, CTRL7_VAL);
    }

    /**
     * Read acceleration from register data.
     *
     * @return the current pressure data.
     */
    AccelerationValue pollAccelerometer() {
        // Read bytes from OUT_X_L_A to OUT_Z_H_A inclusive.
        final byte[] accelerationBytes = device.read(OUT_X_L_A, READ_SIZE);
        // Array is in reverse order after bulk read.
        final ByteBuffer byteBuffer = ByteBuffer.allocate(READ_SIZE);
        for (int i = READ_SIZE - 1; i >= 0; i--) {
            byteBuffer.put(accelerationBytes[i]);
        }
        // Set buffer to be read and set its index to 0.
        byteBuffer.flip();
        final ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        // Array was reversed, so x is now last, y second, and z first.
        final short x = shortBuffer.get(2);
        final short y = shortBuffer.get(1);
        final short z = shortBuffer.get(0);

        return new AccelerationValue(x, y, z);
    }

    /**
     * Read rotational strengths from register data.
     *
     * @return the current pressure data.
     */
    RotationValue pollMagnetometer() {
        // Read bytes from OUT_X_L_M to OUT_Z_H_M inclusive.
        final byte[] rotationBytes = device.read(OUT_X_L_M, READ_SIZE);
        // Array is in reverse order after bulk read.
        final ByteBuffer byteBuffer = ByteBuffer.allocate(READ_SIZE);
        for (int i = READ_SIZE - 1; i >= 0; i--) {
            byteBuffer.put(rotationBytes[i]);
        }
        // Set buffer to be read and set its index to 0.
        byteBuffer.flip();
        final ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        // Array was reversed, so x is now last, y second, and z first.
        final short x = shortBuffer.get(2);
        final short y = shortBuffer.get(1);
        final short z = shortBuffer.get(0);

        return new RotationValue(x, y, z);
    }
}
