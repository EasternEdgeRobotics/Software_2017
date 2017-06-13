package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.devices.Bluetooth;
import com.easternedgerobotics.rov.value.BluetoothValue;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public final class BluetoothReader implements Bluetooth {
    /**
     * Contain the subscription for the bluetooth observable.
     */
    private final CompositeSubscription subscription = new CompositeSubscription();

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

    public BluetoothReader(
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
     * Begin downloading files from remote connections.
     */
    @Override
    public void start(final EventPublisher eventPublisher) {
        final Observable<BluetoothValue> source = Observable.create(new BluetoothReaderSyncOnSubscribe());
        subscription.add(source.subscribeOn(Schedulers.newThread()).subscribe(eventPublisher::emit, Logger::error));
    }

    /**
     * Stop downloading files.
     */
    @Override
    public void stop() {
        subscription.clear();
    }

    private final class BluetoothReaderSyncOnSubscribe extends SyncOnSubscribe<SerialPort, BluetoothValue> {
        /**
         * Create the serial port for the observable.
         *
         * @return the serial port
         */
        @Override
        protected SerialPort generateState() {
            try {
                final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
                final SerialPort serialPort = portIdentifier.open(comPortName, connectionTimeout);
                serialPort.setSerialPortParams(baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
                return serialPort;
            } catch (final NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Read the next line of input from the serial port if it exists.
         *
         * @param serialPort the bluetooth serialPort.
         * @param observer the listener to this receiver.
         * @return the serial port
         */
        @Override
        protected SerialPort next(final SerialPort serialPort, final Observer<? super BluetoothValue> observer) {
            try {
                final InputStream inputStream = serialPort.getInputStream();
                try (final Scanner scanner = new Scanner(inputStream)) {
                    if (scanner.hasNext()) {
                        final String next = scanner.nextLine();
                        if (!next.isEmpty()) {
                            observer.onNext(new BluetoothValue(next));
                        }
                    }
                }
            } catch (final IOException e) {
                observer.onError(e);
            }
            return serialPort;
        }

        /**
         * When the object is unsubscribed, close the serial port.
         *
         * @param serialPort the serial port associated with this instance.
         */
        protected void onUnsubscribe(final SerialPort serialPort) {
            serialPort.close();
        }
    }
}
