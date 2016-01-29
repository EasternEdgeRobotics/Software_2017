package com.easternedgerobotics.rov.control;

// import java.util.StringJoiner;

import com.easternedgerobotics.rov.io.Device;

public class I2CSim implements Device {

    private byte deviceAddress;
    
    private byte[] lastWrite;
    
    private byte[] lastRead;

    public I2CSim(final byte address) {
        this.deviceAddress = address;
    }

    @Override
    public final void write(final byte writeAddress, final byte[] buffer) {
        // transmit to fake device
        // final StringJoiner sj1 = new StringJoiner(", ", "[ ", " ]")
        //     .add("device address: " + String.valueOf(address))
        //     .add("write address: " + String.valueOf(writeAddress));
        // for (byte b : buffer){
        //     sj1.add(String.valueOf(b));
        // }
        // System.out.println(sj1.toString());
        lastWrite = buffer;
    }

    @Override
    public final byte[] read(final byte readAddress, final int readLength) {
        final byte[] readBuffer = new byte[readLength];
        // read from fake device
        lastRead = readBuffer;
        return readBuffer;
    }
    
    public final byte[] getLastWrite() {
        return lastWrite;
    }
    
    public final byte[] getLastRead() {
        return lastRead;
    }
}
