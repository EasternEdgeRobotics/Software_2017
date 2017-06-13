package com.easternedgerobotics.rov.io.devices;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MockI2C implements I2C {
    private Map<Byte, Byte> storage = new ConcurrentHashMap<>();

    @Override
    public void write(final byte writeAddress, final byte value) {
        storage.put(writeAddress, value);
    }

    @Override
    public void write(final byte writeAddress, final byte[] buffer) {
        for (byte i = 0; i < buffer.length; i++) {
            write((byte) (writeAddress + i), buffer[i]);
        }
    }

    @Override
    public byte read(final byte readAddress) {
        return storage.computeIfAbsent(readAddress, i -> (byte) 0);
    }

    @Override
    public byte[] read(final byte readAddress, final int readLength) {
        final byte[] result = new byte[readLength];
        for (byte i = 0; i < readLength; i++) {
            result[i] = read((byte) (readAddress + i));
        }
        return result;
    }
}
