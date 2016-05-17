package com.easternedgerobotics.rov.io.firmata;

import com.bortbort.arduino.FiloFirmata.Messages.TransmittableChannelMessage;
import com.bortbort.arduino.FiloFirmata.Parser.CommandBytes;

import java.io.ByteArrayOutputStream;

/**
 * Report Digital Channel Message.
 * Asks the Firmata Device to start reporting Digital values for the given channel (pin)
 */
public final class ReportStandardDigitalPortMessage extends TransmittableChannelMessage {

    private final boolean enableReporting;

    /**
     * Report Digital Channel Message.
     *
     * @param channelGroup group of 8 ports.
     * @param enableReporting Boolean value to enable or disable reporting. (True = enable)
     */
    public ReportStandardDigitalPortMessage(final int channelGroup, final boolean enableReporting) {
        super(CommandBytes.REPORT_DIGITAL_PIN, channelGroup);
        this.enableReporting = enableReporting;
    }

    @Override
    protected Boolean serialize(final ByteArrayOutputStream outputStream) {
        if (enableReporting) {
            outputStream.write((byte) 0x01);
        } else {
            outputStream.write((byte) 0x00);
        }
        return true;
    }
}
