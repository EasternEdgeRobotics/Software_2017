package com.easternedgerobotics.rov.io;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public final class SerialWriter {
    /**
     * Returns a SerialWriter associated with the given port.
     *
     * @param serialPort the serial port
     * @return a SerialWriter instance
     */
    public static SerialWriter create(final Serial serialPort) {
        return new SerialWriter(serialPort);
    }

    private final Observable<SerialConnection> obs;

    private SerialWriter(final Serial serialPort) {
        obs = Observable.create(new OnSubscribe<SerialConnection>() {
            @Override
            public void call(final Subscriber<? super SerialConnection> subscriber) {
                subscriber.onNext(serialPort);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<SerialConnection> connect() {
        return obs;
    }
}
