package com.easternedgerobotics.rov.io.arduino;

import com.easternedgerobotics.rov.value.AnalogPinValue;
import com.easternedgerobotics.rov.value.ArduinoHeartbeatValue;
import com.easternedgerobotics.rov.value.DigitalPinValue;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;

final class ArduinoReader implements SerialPortEventListener {
    /**
     * Indicates the start of a serial message.
     */
    private static final int MESSAGE_START = 0xAA;

    /**
     * Indicates a message which represents the newest value of a digital pin.
     */
    private static final int DIGITAL_CHANGE = 0;

    /**
     * The size in bytes of a digital change message.
     */
    private static final int DIGITAL_CHANGE_SIZE = 2;

    /**
     * Indicates a message which represents the newest value of an analog pin.
     */
    private static final int ANALOG_CHANGE = 1;

    /**
     * The size in bytes of an analog change message.
     */
    private static final int ANALOG_CHANGE_SIZE = 3;

    /**
     * Maximum value from an analog channel.
     */
    private static final float ANALOG_RESOLUTION = 1023f;

    /**
     * Indicates a message which proves the device is connected.
     */
    private static final int HEARTBEAT = 2;

    /**
     * Publisher of values reported by the arduino.
     */
    private final PublishSubject<Object> subject = PublishSubject.create();

    /**
     * A map of observables already linked to subject.
     */
    private final ConcurrentHashMap<Class, Object> values = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return (Observable<T>) values.computeIfAbsent(clazz, c -> subject.filter(c::isInstance).cast(c));
    }

    /**
     * When data becomes available, arduino messages will be parsed and converted to java objects.
     *
     * @param event RXTX serial event
     */
    @Override
    public void serialEvent(final SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            final SerialPort port = (SerialPort) event.getSource();
            try {
                final InputStream input = port.getInputStream();
                while (input.available() > 0) {
                    if (input.read() == MESSAGE_START) {
                        final int type = input.read();
                        switch (type) {
                            case DIGITAL_CHANGE:
                                final byte[] digitalBytes = readBytes(input, DIGITAL_CHANGE_SIZE);
                                subject.onNext(new DigitalPinValue(digitalBytes[0], digitalBytes[1] > 0));
                                break;
                            case ANALOG_CHANGE:
                                final byte[] analogBytes = readBytes(input, ANALOG_CHANGE_SIZE);
                                final short value = ByteBuffer.wrap(new byte[]{analogBytes[1], analogBytes[2]})
                                    .order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(0);
                                subject.onNext(new AnalogPinValue(analogBytes[0], value / ANALOG_RESOLUTION));
                                break;
                            case HEARTBEAT:
                                subject.onNext(new ArduinoHeartbeatValue(input.read() > 0));
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (final IOException e) {
                Logger.warn("Error while parsing Arduino message: {}", e);
            }
        }
    }

    /**
     * Read bytes continuously until the desired amount has been collected.
     *
     * @param input the byte source.
     * @param size number of bytes to collect.
     * @return byte array.
     * @throws IOException if reading from input fails.
     */
    private static byte[] readBytes(final InputStream input, final int size) throws IOException {
        final byte[] bytes = new byte[size];
        int bytesRemaining = size;
        int offset = 0;
        while (bytesRemaining > 0) {
            final int readCount = input.read(bytes, offset, bytesRemaining);
            if (readCount > 0) {
                offset += readCount;
                bytesRemaining -= readCount;
            }
        }
        return bytes;
    }
}
