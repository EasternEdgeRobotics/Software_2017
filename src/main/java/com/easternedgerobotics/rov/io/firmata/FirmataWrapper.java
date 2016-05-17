package com.easternedgerobotics.rov.io.firmata;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.Messages.SysexReportFirmwareMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexReportFirmwareQueryMessage;

import java.util.Objects;

final class FirmataWrapper {

    /**
     * The serial com connected to the Firmata device.
     */
    private final String com;

    /**
     * The baud rate of the Firmata device.
     */
    private final int baud;

    /**
     * The wrapped Firmata object.
     */
    private Firmata firmata;

    /**
     * A wrapper for a Firmata object.
     *
     * @param com
     * @param baud
     */
    FirmataWrapper(final String com, final int baud) {
        this.com = com;
        this.baud = baud;
    }

    /**
     * Ask the firmata version to see if the device is connected.
     *
     * @return true if the device is connected
     */
    boolean isConnected() {
        return !Objects.isNull(firmata)
            && !Objects.isNull(firmata.sendMessageSynchronous(SysexReportFirmwareMessage.class,
                new SysexReportFirmwareQueryMessage()));
    }

    /**
     * Replace the wrapped Firmata object with a new one and open the serial port.
     *
     * @return true if the serial connection could be started
     */
    boolean start() {
        if (!Objects.isNull(firmata)) {
            firmata.stop();
        }
        firmata = new Firmata(com, baud);
        return firmata.start();
    }

    /**
     * Get the wrapped Firmata object.
     *
     * @return firmata
     */
    Firmata get() {
        return firmata;
    }
}
