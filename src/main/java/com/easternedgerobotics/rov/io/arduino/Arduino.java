package com.easternedgerobotics.rov.io.arduino;

import com.easternedgerobotics.rov.value.AnalogPinValue;
import com.easternedgerobotics.rov.value.DigitalPinValue;
import com.easternedgerobotics.rov.value.HeartbeatValue;

import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class Arduino {
    /**
     * Controls access to a serialPort connected to an Arduino.
     */
    private final ArduinoPort port;

    /**
     * Pins to be configured as inputs.
     */
    private final byte[] inputs;

    /**
     * Pins to be configured as outputs.
     */
    private final byte[] outputs;

    /**
     * Pins to be configured as input pullups.
     */
    private final byte[] inputPullups;

    /**
     * Manage incoming data from the arduino serial port.
     */
    private final ArduinoReader reader = new ArduinoReader();

    /**
     * Manage outgoing data to the arduino serial port.
     */
    private final ArduinoWriter writer = new ArduinoWriter();

    /**
     * Subscriptions to arduino heartbeats which trigger re-connect attempts to the device.
     */
    private final CompositeSubscription heartbeatSubscription = new CompositeSubscription();

    /**
     * Create an arduino interface to a specific serial interface, and pin configuration.
     *
     * @param port the serial interface.
     * @param inputs pins to be configured as inputs.
     * @param outputs pins to be configured as outputs pins.
     * @param inputPullups pins to be configured as input pullups.
     */
    public Arduino(
        final ArduinoPort port,
        final byte[] inputs,
        final byte[] outputs,
        final byte[] inputPullups
    ) {
        this.port = port;
        this.inputs = inputs.clone();
        this.outputs = outputs.clone();
        this.inputPullups = inputPullups.clone();
    }

    /**
     * Start a connection with the arduino device.
     * Attempts to recreate the connection if the device stops responding.
     *
     * @param heartRate the interval at which heartbeats are sent to the arduino.
     * @param heartbeatTimeout the maximum gap between receiving a heartbeat before considering the connection dead.
     * @param unit the unit for @heartRate and @heartbeatTimeout
     */
    public void start(
        final int heartRate,
        final int heartbeatTimeout,
        final TimeUnit unit
    ) {
        heartbeatSubscription.add(Observable.interval(heartRate, unit)
            .observeOn(Schedulers.io())
            .subscribe(tick -> writer.requestHeartbeat()));

        // Inject message when timeout is reached.
        final Observable<HeartbeatValue> timeout = Observable.just(new HeartbeatValue(false))
            .delay(heartbeatTimeout, unit)
            .doOnNext(heartbeat -> Logger.warn("Arduino timeout while waiting for heartbeat"))
            .repeat();

        final Observable<HeartbeatValue> heartbeats = reader.valuesOfType(HeartbeatValue.class);

        // When time out is reached, reconnect reader and writer to a new serial port instance.
        heartbeatSubscription.add(timeout.takeUntil(heartbeats).repeat().mergeWith(heartbeats)
            .startWith(new HeartbeatValue(false))
            .filter(beat -> !beat.getOperational())
            .observeOn(Schedulers.io())
            .subscribe(heartbeatValue -> init()));
    }

    /**
     * Refresh the serial port, attach the reader and writer, and load the pin configuration.
     */
    private void init() {
        try {
            port.close();
            port.open();
            port.attach(reader);
            port.attach(writer);
            for (final byte pin : inputs) {
                writer.setPinInputMode(pin);
            }
            for (final byte pin : outputs) {
                writer.setPinOutputMode(pin);
            }
            for (final byte pin : inputPullups) {
                writer.setPinInputPullupMode(pin);
            }
            writer.requestAllPinValues();
        } catch (final IOException e) {
            Logger.warn("Error while creating Arduino connection: {}", e);
            port.close();
        }
    }

    /**
     * Close the serial port and disable re-connect attempts.
     */
    public void stop() {
        heartbeatSubscription.clear();
        port.close();
    }

    /**
     * Observe change events from a specific digital pin.
     *
     * @param address the physical location of the pin.
     * @return Observable
     */
    public Observable<DigitalPinValue> digitalPin(final byte address) {
        return reader.valuesOfType(DigitalPinValue.class).filter(pin -> pin.getAddress() == address);
    }

    /**
     * Observe change events from a specific analog pin.
     *
     * @param address the physical location of the pin.
     * @return Observable
     */
    public Observable<AnalogPinValue> analogPin(final byte address) {
        return reader.valuesOfType(AnalogPinValue.class).filter(pin -> pin.getAddress() == address);
    }

    /**
     * Set the value of a digital pin.
     *
     * @param address the physical location of the pin.
     * @param value the desired state of the pin.
     */
    public void setPinValue(final byte address, final boolean value) {
        writer.setPinValue(address, value);
    }
}
