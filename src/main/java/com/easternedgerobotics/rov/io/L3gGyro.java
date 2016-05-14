package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.AngularVelocityValue;

import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.TimeUnit;

public class L3gGyro {

    /**
     * The L3GD20H I2C addresses SA0 is connected to power (high).
     */
    public static final byte SA0_HIGH_ADDRESS = 0b01101011;

    /**
     * The L3GD20H I2C addresses SA0 is grounded (low).
     */
    public static final byte SA0_LOW_ADDRESS = 0b01101010;

    /**
     * Bit(3) in L3gGyroReg.CTRL1.
     */
    private static final byte MODE = 0x08;

    /**
     * Bit(2) in L3gGyroReg.CTRL1.
     */
    private static final byte X_ENABLE = 0x04;


    /**
     * Bit(1) in L3gGyroReg.CTRL1.
     */
    private static final byte Y_ENABLE = 0x02;


    /**
     * Bit(0) in L3gGyroReg.CTRL1.
     */
    private static final byte Z_ENABLE = 0x01;

    /**
     * The expected WHO_AM_I register value for L3GD20H gryo.
     */
    private static final byte WHO_AM_I_VAL = (byte) 0xD7;

    /**
     * The span of registers holding data.
     */
    private static final int READ_SIZE = 6;

    private final Observable<Long> interval;

    private final Device device;

    public L3gGyro(final Device device, final long sleepDuration, final TimeUnit timeUnit) {
        this.device = device;
        interval = Observable.interval(sleepDuration, timeUnit);
    }


    /**
     * Set the device to "Normal" mode and enable x, y, and z axis.
     *
     * @return true if no i2c errors
     */
    public final boolean enableAll() {
        return enable(true, true, true, true);
    }

    /**
     * Set the enabled state of the L3GD20H.
     *
     * @param normalMode false for "Power Down" mode and true for "Normal" mode.
     * @param xEnable enable or disable updates in the x axis.
     * @param yEnable enable or disable updates in the y axis.
     * @param zEnable enable or disable updates in the z axis.
     * @return true if no i2c errors
     */
    public final boolean enable(
        final boolean normalMode,
        final boolean xEnable,
        final boolean yEnable,
        final boolean zEnable
    ) {
        try {
            // keep the original byte so the higher nibble is not overwritten.
            byte ctrl1 = device.read(L3gGyroReg.CTRL1.getAddress(), 1)[0];
            // set bits in the lower nibble of the ctrl1 reg.
            if (normalMode) {
                ctrl1 = setBitsHigh(ctrl1, MODE);
            } else {
                ctrl1 = setBitsLow(ctrl1, MODE);
            }

            if (xEnable) {
                ctrl1 = setBitsHigh(ctrl1, X_ENABLE);
            } else {
                ctrl1 = setBitsLow(ctrl1, X_ENABLE);
            }

            if (yEnable) {
                ctrl1 = setBitsHigh(ctrl1, Y_ENABLE);
            } else {
                ctrl1 = setBitsLow(ctrl1, Y_ENABLE);
            }

            if (zEnable) {
                ctrl1 = setBitsHigh(ctrl1, Z_ENABLE);
            } else {
                ctrl1 = setBitsLow(ctrl1, Z_ENABLE);
            }
            // overwrite the old value.
            device.write(L3gGyroReg.CTRL1.getAddress(), new byte[]{ctrl1});

            return true;
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return value where each bit is set high if the corresponding bit in the mask is high.
     *
     * @param value the value to modify.
     * @param mask the bits to set.
     * @return the modified value.
     */
    private static byte setBitsHigh(final byte value, final byte mask) {
        return (byte) (value | mask);
    }


    /**
     * Return value where each bit is set low if the corresponding bit in the mask is high.
     *
     * @param value the value to modify.
     * @param mask the bits to set.
     * @return the modified value.
     */
    private static byte setBitsLow(final byte value, final byte mask) {
        return (byte) (value & ~mask);
    }

    /**
     * Verify the connection to the L3GD20H.
     *
     * @return true if the device is an L3GD20H and is addressed properly and has a valid connection.
     */
    public final boolean verifyConnection() {
        try {
            final byte response = device.read(L3gGyroReg.WHO_AM_I.getAddress(), 1)[0];
            return (response == WHO_AM_I_VAL);
        } catch (final IOException e) {
            return false;
        }
    }

    public final Observable<AngularVelocityValue> angularVelocity() {
        return interval.map(this::pollAngularVelocity);
    }

    private AngularVelocityValue pollAngularVelocity(final long tick) {
        try {
            // Read bytes from OUT_X_L to OUT_Z_H inclusive.
            final byte[] velocityBytes = device.read(L3gGyroReg.OUT_X_L.getAddress(), READ_SIZE);
            // Array is in reverse order after bulk read.
            final ByteBuffer byteBuffer = ByteBuffer.allocate(READ_SIZE);
            for (int i = READ_SIZE - 1; i  >=  0; i--) {
                byteBuffer.put(velocityBytes[i]);
            }
            // Set buffer to be read and set its index to 0.
            byteBuffer.flip();
            final ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
            // Array was reversed, so x is now last, y second, and z third.
            final short x = shortBuffer.get(2);
            final short y = shortBuffer.get(1);
            final short z = shortBuffer.get(0);

            return AngularVelocityValue.create(x, y, z);

        } catch (final IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
