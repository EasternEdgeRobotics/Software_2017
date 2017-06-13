package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.devices.I2C;
import com.easternedgerobotics.rov.value.AngularVelocityValue;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

final class L3GD20H {
    /**
     * Register used to control Angular Velocity update state.
     */
    static final byte CTRL1 = (byte) 0x20;

    /**
     * A value which enables Angular Velocity updates.
     */
    static final byte CTRL1_ENABLE = (byte) 0xFF;

    /**
     * The span of registers holding data.
     */
    static final int READ_SIZE = 6;

    /**
     * The start register for Angular Velocity data.
     */
    static final byte OUT_X_L = (byte) 0x28;

    /**
     * An i2c interface connected to the sensor.
     */
    private final I2C device;

    /**
     * Read from an L3GD20H to receive Angular Velocity data.
     *
     * @param device the IMU chip I2C interface.
     */
    L3GD20H(final I2C device) {
        this.device = device;
        device.write(CTRL1, CTRL1_ENABLE);
    }

    /**
     * Read angular velocity from register data.
     *
     * @return the current angular velocity data.
     */
    AngularVelocityValue pollAngularVelocity() {
        // Read bytes from OUT_X_L to OUT_Z_H inclusive.
        final byte[] velocityBytes = device.read(OUT_X_L, READ_SIZE);
        // Array is in reverse order after bulk read.
        final ByteBuffer byteBuffer = ByteBuffer.allocate(READ_SIZE);
        for (int i = READ_SIZE - 1; i >= 0; i--) {
            byteBuffer.put(velocityBytes[i]);
        }
        // Set buffer to be read and set its index to 0.
        byteBuffer.flip();
        final ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        // Array was reversed, so x is now last, y second, and z first.
        final short x = shortBuffer.get(2);
        final short y = shortBuffer.get(1);
        final short z = shortBuffer.get(0);

        return new AngularVelocityValue(x, y, z);
    }
}
