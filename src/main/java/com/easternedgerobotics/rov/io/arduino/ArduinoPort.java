package com.easternedgerobotics.rov.io.arduino;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Subscription;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;

public final class ArduinoPort {
    /**
     * Name given to the com device.
     */
    private final String comPortName;

    /**
     * Location/name of the com port.
     */
    private final String comPort;

    /**
     * Maximum time to wait for the device.
     */
    private final int connectionTimeout;

    /**
     * Communication rate for the device.
     */
    private final int baudRate;

    /**
     * The active serial port instance.
     */
    private SerialPort serialPort;

    /**
     * Link between an active writer and the serial output stream.
     */
    private Subscription outputSubscription;

    /**
     * Represents a com port to be used by a connected Arduino.
     *
     * @param comPortName Name given to the com device.
     * @param comPort Maximum time to wait for the device.
     * @param connectionTimeout Maximum time to wait for the device.
     * @param baudRate Communication rate for the device.
     */
    public ArduinoPort(
        final String comPortName,
        final String comPort,
        final int connectionTimeout,
        final int baudRate
    ) {
        this.comPortName = comPortName;
        this.comPort = comPort;
        this.connectionTimeout = connectionTimeout;
        this.baudRate = baudRate;
    }

    /**
     * Attempt to init a serial connection with the arduino.
     * Blocks for connectionTimeout milliseconds until the start-bit is received to avoid corrupting the serial line.
     *
     * @throws IOException if the connection can not be made.
     */
    void open() throws IOException {
        try {
            final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
            serialPort = portIdentifier.open(comPortName, connectionTimeout);
            serialPort.setSerialPortParams(baudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

            // Should not actually error out unless something is totally whack.
            final Observable<Object> connectionAttempt = Observable.create(subscriber -> {
                try {
                    final InputStream in = serialPort.getInputStream();
                    while (!subscriber.isUnsubscribed()) {
                        if (in.read() != -1) {
                            subscriber.onCompleted();
                        }
                    }
                } catch (final IOException e) {
                    throw Exceptions.propagate(e);
                }
            });

            connectionAttempt.timeout(connectionTimeout, TimeUnit.MILLISECONDS).toBlocking().subscribe(
                ignored -> { },
                error -> Logger.warn("Failed to init to device on com {}: {}", comPort, error),
                () -> Logger.info("Connected  to device on com {}", comPort)
            );

        } catch (final NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
            throw new IOException(e);
        }
    }

    /**
     * Attach an event listener to the serial port object.
     *
     * @param reader handles serial port events
     * @throws IOException if there are already listeners connected.
     */
    void attach(final ArduinoReader reader) throws IOException {
        try {
            serialPort.addEventListener(reader);
            serialPort.notifyOnDataAvailable(true);
        } catch (final TooManyListenersException e) {
            throw new IOException(e);
        }
    }

    /**
     * Subscribe the output stream of the serial port to an arduino writer with properly formatted messages.
     *
     * @param writer the writing object
     * @throws IOException if the output stream can not be opened.
     */
    void attach(final ArduinoWriter writer) throws IOException {
        final OutputStream outputStream = serialPort.getOutputStream();
        outputSubscription = writer.getOutput().subscribe(bytes -> {
            try {
                outputStream.write(bytes);
            } catch (final IOException e) {
                close();
            }
        });
    }

    /**
     * Close the serial port, remove the event listeners and un-subscribe the output stream.
     */
    void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            serialPort = null;
        }
        if (outputSubscription != null) {
            outputSubscription.unsubscribe();
            outputSubscription = null;
        }
    }
}
