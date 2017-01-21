package com.easternedgerobotics.rov.io.pololu;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

final class IMUI2C {
    /**
     * Set this bit high in the register address to enable multiple byte reads.
     */
    private static final byte MULTI_READ_ENABLE = (byte) 0x80;

    /**
     * The I2C interface connected to an IMU chip.
     */
    private I2CDevice device;

    /**
     * Read from an IMU chip.
     *
     * @param device the base I2C interface.
     */
    IMUI2C(final I2CDevice device) {
        this.device = device;
    }

    /**
     * Write a single byte to the IMU chip.
     *
     * @param writeAddress the register address
     * @param value the byte to be written
     * @throws IOException if there is an I2C error during the transfer.
     */
    void write(final byte writeAddress, final byte value) throws IOException {
        device.write(writeAddress, value);
    }

    /**
     * Write multiple bytes to the IMU chip.
     *
     * @param writeAddress the register address
     * @param buffer the bytes to be written
     * @throws IOException if there is an I2C error during the transfer.
     */
    void write(final byte writeAddress, final byte[] buffer) throws IOException {
        device.write(writeAddress, buffer, 0, buffer.length);
    }

    /**
     * Read a single byte from the IMU chip.
     *
     * @param readAddress the register address
     * @return the byte received
     * @throws IOException if there is an I2C error during the transfer.
     */
    byte read(final byte readAddress) throws IOException {
        return (byte) device.read(readAddress);
    }

    /**
     * Read multiple bytes from the IMU chip.
     *
     * @param readAddress the starting register address
     * @param readLength the amount of bytes to read
     * @return the bytes received
     * @throws IOException if there is an I2C error during the transfer.
     */
    byte[] read(final byte readAddress, final int readLength) throws IOException {
        final byte[] readBuffer = new byte[readLength];
        device.read(multiReadAddress(readAddress), readBuffer, 0, readBuffer.length);
        return readBuffer;
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
