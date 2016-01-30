package com.easternedgerobotics.rov.io;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

public class I2C implements Device {
    private I2CDevice i2cDevice;

    public I2C(final I2CDevice device) {
        i2cDevice = device;
    }

    @Override
    public final void write(final byte writeAddress, final byte[] buffer) throws IOException {
        i2cDevice.write(writeAddress, buffer, 0, buffer.length);
    }

    @Override
    public final byte[] read(final byte readAddress, final int readLength) throws IOException {
        final byte[] readBuffer = new byte[readLength];
        i2cDevice.read(readAddress, readBuffer, 0, readBuffer.length);
        return readBuffer;
    }
}
