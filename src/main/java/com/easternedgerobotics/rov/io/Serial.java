package com.easternedgerobotics.rov.io;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Serial implements SerialConnection {
    /**
     * The default speed (baud rate) in bits per second.
     */
    private static final int DEFAULT_SPEED_BPS = 57600;

    /**
     * The default timeout in milliseconds.
     */
    private static final int DEFAULT_TIMEOUT_MS = 5000;

    private String name;

    private int speed;

    private int timeout;

    private SerialPort serialPort;

    private InputStream input;

    private OutputStream output;

    private boolean isConnected;

    public Serial(final String portName, final int portSpeed, final int portTimeout) {
        this.name = portName;
        this.speed = portSpeed;
        this.timeout = portTimeout;
        this.isConnected = false;
    }

    public Serial(final String portName) {
        this(portName, DEFAULT_SPEED_BPS, DEFAULT_TIMEOUT_MS);
    }

    public final InputStream getInputStream() {
        return input;
    }

    public final void writeBytes(final byte[] bytes) throws IOException {
        if (isConnected) {
            output.write(bytes);
        }
    }

    public final void connect()
        throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {

        final CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(name);
        serialPort = (SerialPort) portId.open("App", timeout);
        serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        input = serialPort.getInputStream();
        output = serialPort.getOutputStream();
        isConnected = true;
    }

    public final void disconnect() throws IOException {
        if (isConnected) {
            input.close();
            output.close();
            serialPort.close();
        }
        isConnected = false;
    }
}
