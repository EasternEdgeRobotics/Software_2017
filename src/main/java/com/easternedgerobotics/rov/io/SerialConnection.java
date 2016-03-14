package com.easternedgerobotics.rov.io;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialPortException;
import rx.Observable;
import rx.subjects.PublishSubject;

public final class SerialConnection {

    private final PublishSubject<SerialDataEvent> dataSubject;

    public Observable<String> asStrings() {
        return dataSubject
            .map(SerialDataEvent::getData)
            .cast(String.class)
            .share();
    }

    public Observable<Byte[]> asBytes() {
        return dataSubject
            .map(SerialDataEvent::getData)
            .map(String::getBytes)
            .cast(Byte[].class)
            .share();
    }

    private final Serial serial;

    private final String device;

    private final int baud;

    private final SerialDataListener dataListener = new SerialDataListener() {
        @Override
        public void dataReceived(final SerialDataEvent event) {
            dataSubject.onNext(event);
        }
    };

    public SerialConnection(final Serial serial, final String device, final int baud) {
        dataSubject = PublishSubject.create();
        this.serial = serial;
        this.device = device;
        this.baud = baud;
    }

    public void open() {
        try {
            serial.open(device, baud);
            serial.addListener(dataListener);
        } catch (final SerialPortException e) {
            close();
        }
    }

    public void close() {
        try {
            if (serial.isOpen()) {
                serial.close();
            }
        } catch (final IllegalStateException ignored) {

        }
        serial.removeListener(dataListener);
    }

    public void write(final byte[] payload) {
        try {
            if (serial.isOpen()) {
                serial.write(payload);
            }
        } catch (final IllegalStateException e) {
            close();
        }
    }

    public void write(final byte payload) {
        try {
            if (serial.isOpen()) {
                serial.write(payload);
            }
        } catch (final IllegalStateException e) {
            close();
        }
    }

    public boolean isOpen() {
        return serial.isOpen();
    }
}
