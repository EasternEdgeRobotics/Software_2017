package com.easternedgerobotics.rov.io;

import java.io.IOException;

public interface SerialConnection {
    void writeBytes(final byte[] bytes) throws IOException;

    void disconnect() throws IOException;
}
