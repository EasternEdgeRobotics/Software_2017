package com.easternedgerobotics.rov.io;

import java.io.IOException;

public interface Device {
    void write(byte writeAddress, byte[] buffer) throws IOException;

    byte[] read(byte readAddress, int readLength) throws IOException;
}
