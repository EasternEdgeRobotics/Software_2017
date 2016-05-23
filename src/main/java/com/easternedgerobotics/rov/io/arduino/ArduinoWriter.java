package com.easternedgerobotics.rov.io.arduino;

import rx.Observable;
import rx.subjects.PublishSubject;

final class ArduinoWriter {
    /**
     * Indicates the start of a serial message.
     */
    private static final byte MESSAGE_START = (byte) 0xAA;

    /**
     * Indicates the start of a write digital value message.
     */
    private static final byte WRITE_DIGITAL_VAL = 0x01;

    /**
     * Indicates the start of a write digital mode message.
     */
    private static final byte WRITE_DIGITAL_MODE = 0x02;

    /**
     * Select input mode option when setting digital pin mode.
     */
    private static final byte INPUT_MODE = 0x01;

    /**
     * Select output mode option when setting digital pin mode.
     */
    private static final byte OUTPUT_MODE = 0x02;

    /**
     * Select input pullup mode option when setting digital pin mode.
     */
    private static final byte INPUT_PULLUP_MODE = 0x03;

    /**
     * Indicates the start of a heartbeat request message.
     */
    private static final byte HEARTBEAT_REQUEST = 0x03;

    /**
     * The output stream connected to the mega.
     */
    private final PublishSubject<byte[]> out = PublishSubject.create();

    Observable<byte[]> getOutput() {
        return out;
    }

    /**
     * Tell the arduino to respond with a heartbeat to prove that the connection still exists.
     */
    void requestHeartbeat() {
        out.onNext(new byte[]{MESSAGE_START, HEARTBEAT_REQUEST});
    }

    /**
     * Set the value of a digital pin.
     *
     * @param address the physical location of the pin.
     * @param value the desiered state of the pin.
     */
    void setPinValue(final byte address, final boolean value) {
        if (value) {
            out.onNext(new byte[]{MESSAGE_START, WRITE_DIGITAL_VAL, address, 1});
        } else {
            out.onNext(new byte[]{MESSAGE_START, WRITE_DIGITAL_VAL, address, 0});
        }
    }

    /**
     * Set the mode of a pin to output.
     *
     * @param address the physical location of the pin.
     */
    void setPinOutputMode(final byte address) {
        out.onNext(new byte[]{MESSAGE_START, WRITE_DIGITAL_MODE, address, OUTPUT_MODE});
    }

    /**
     * Set the mode of a pin to input.
     *
     * @param address the physical location of the pin.
     */
    void setPinInputMode(final byte address) {
        out.onNext(new byte[]{MESSAGE_START, WRITE_DIGITAL_MODE, address, INPUT_MODE});
    }

    /**
     * Set the mode of a pin to input pullup.
     *
     * @param address the physical location of the pin.
     */
    void setPinInputPullupMode(final byte address) {
        out.onNext(new byte[]{MESSAGE_START, WRITE_DIGITAL_MODE, address, INPUT_PULLUP_MODE});
    }
}
