package com.easternedgerobotics.rov.io.rpi;

import com.easternedgerobotics.rov.io.I2C;

import com.pi4j.io.i2c.I2CDevice;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

final class RaspberryI2C implements I2C {
    /**
     * Set this bit high in the register address to enable multiple byte reads.
     */
    private static final byte MULTI_READ_ENABLE = (byte) 0x80;

    /**
     * The I2C interface connected to a raspberry pi i2c device.
     */
    private I2CDevice device;

    /**
     * Used for i2c concurrency safety.
     */
    private final Lock lock;

    /**
     * Read from a raspberry pi i2c device.
     *
     * @param device the base I2C interface.
     * @param lock concurrency safety.
     */
    RaspberryI2C(final I2CDevice device, final Lock lock) {
        this.device = device;
        this.lock = lock;
    }

    @Override
    public void write(final byte writeAddress, final byte value) {
        lock.lock();
        try {
            device.write(writeAddress, value);
        } catch (final IOException e) {
            Logger.warn(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(final byte writeAddress, final byte[] buffer) {
        lock.lock();
        try {
            device.write(writeAddress, buffer, 0, buffer.length);
        } catch (final IOException e) {
            Logger.warn(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public byte read(final byte readAddress) {
        lock.lock();
        try {
            return (byte) device.read(readAddress);
        } catch (final IOException e) {
            Logger.warn(e);
            return 0;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public byte[] read(final byte readAddress, final int readLength) {
        lock.lock();
        try {
            final byte[] readBuffer = new byte[readLength];
            device.read(multiReadAddress(readAddress), readBuffer, 0, readBuffer.length);
            return readBuffer;
        } catch (final IOException e) {
            Logger.warn(e);
            return new byte[readLength];
        } finally {
            lock.unlock();
        }
    }

    /**
     * This device requires the most significant bit in the address
     * to be set high when reading multiple bytes at once.
     *
     * @param value the original address
     * @return the multi-read enabled address
     */
    private static byte multiReadAddress(final byte value) {
        return setBitsHigh(value, MULTI_READ_ENABLE);
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
}
