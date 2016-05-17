package com.easternedgerobotics.rov.io.firmata;

import com.bortbort.arduino.FiloFirmata.Firmata;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public final class FirmataObserver {

    private FirmataObserver() {
    }

    /**
     * Observer a serial port for Firmata device connections.
     *
     * @param com the com port of the Firmata device/
     * @param baud the baud rate of the serial connection.
     * @param refreshTime how often the device polled for activity.
     * @param unit the unit for refreshTime
     * @return observable
     */
    public static Observable<Firmata> observe(
        final String com,
        final int baud,
        final long refreshTime,
        final TimeUnit unit
    ) {
        return Observable
            .combineLatest(
                Observable.just(new FirmataWrapper(com, baud)),
                Observable.interval(refreshTime, unit),
                (wrapper, tick) -> wrapper)
            .filter(wrapper -> !wrapper.isConnected())
            .filter(FirmataWrapper::start)
            .filter(FirmataWrapper::isConnected)
            .map(FirmataWrapper::get);
    }
}
