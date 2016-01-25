package com.easternedgerobotics.rov.io;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.io.InputStream;

public class SerialReader {
    protected SerialReader() { }

    public static Observable<byte[]> create(final Serial serialPort) {
        final Observable<byte[]> dataStream = Observable.create(new SerialPortSubscriber(serialPort));

        return dataStream.subscribeOn(Schedulers.io());
    }

    private static final int MAX_BYTE = 255;

    private static class SerialPortSubscriber implements OnSubscribe<byte[]> {
        private final Serial serial;

        public SerialPortSubscriber(final Serial serialPort) {
            this.serial = serialPort;
        }

        @Override
        public void call(final Subscriber<? super byte[]> subscriber) {
            try {
                final InputStream input = serial.getInputStream();
                while (!subscriber.isUnsubscribed()) {
                    final int available = input.available();
                    if (available < 0) {
                        subscriber.onCompleted();
                        break;
                    }
                    if (available < 1) {
                        continue;
                    }

                    final byte[] buffer = new byte[available];
                    final int readCount = input.read(buffer, 0, available);
                    // Adjust from C to Java???
                    for (int i = 0; i < readCount; i++) {
                        if (buffer[i] < 0) {
                            buffer[i] += MAX_BYTE;
                        }
                    }

                    if (readCount < available) {
                        final byte[] read = new byte[readCount];
                        System.arraycopy(buffer, 0, read, 0, readCount);
                        subscriber.onNext(read);
                    } else {
                        subscriber.onNext(buffer);
                    }
                }

                serial.disconnect();
                subscriber.onCompleted();
            } catch (final IOException e) {
                subscriber.onError(e);
            }
        }
    }
}
