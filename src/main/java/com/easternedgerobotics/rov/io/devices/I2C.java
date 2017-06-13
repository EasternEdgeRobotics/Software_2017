package com.easternedgerobotics.rov.io.devices;

public interface I2C {
    /**
     * Write a single byte to the i2c device.
     *
     * @param value the byte to be written
     */
    default void write(final byte value) {

    }

    /**
     * Write a single byte to the i2c device at the given address.
     *
     * @param writeAddress the register address
     * @param value the byte to be written
     */
    default void write(final byte writeAddress, final byte value) {

    }

    /**
     * Write multiple bytes to the i2c device.
     *
     * @param writeAddress the register address
     * @param buffer the bytes to be written
     */
    default void write(final byte writeAddress, final byte[] buffer) {

    }

    /**
     * Read a single byte from the i2c device.
     *
     * @param readAddress the register address
     * @return the byte received
     */
    default byte read(final byte readAddress) {
        return 0;
    }

    /**
     * Read multiple bytes from the i2c device.
     *
     * @param readAddress the starting register address
     * @param readLength the amount of bytes to read
     * @return the bytes received
     */
    default byte[] read(final byte readAddress, final int readLength) {
        return new byte[readLength];
    }
}
