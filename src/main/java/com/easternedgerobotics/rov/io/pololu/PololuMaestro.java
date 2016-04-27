package com.easternedgerobotics.rov.io.pololu;

import com.pi4j.io.serial.Serial;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

/**
 * This class implements the <a href="https://www.pololu.com/docs/0J40/5">Pololu Maestro Servo Controller's serial
 * interface</a>. It is assumed that the connected Maestro is configured in USB Dual Port mode.
 */
public class PololuMaestro {
    /**
     * The mask used to clear a command byte's most significant bit.
     */
    private static final byte COMMAND_MASK = 0x7F;

    /**
     * The first command byte for the Pololu protocol.
     */
    private static final byte START_BYTE = (byte) 0xAA;

    /**
     * The Set Target command byte with its most significant bit cleared.
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    private static final byte COMMAND_SET_TARGET = 0x84 & COMMAND_MASK;

    /**
     * The Get Errors command byte with its most significant bit cleared.
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    private static final byte COMMAND_GET_ERRORS = 0xA1 & COMMAND_MASK;

    /**
     * The Get Position command byte with its most significant bit cleared.
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    private static final byte COMMAND_GET_POSITION = 0x90 & COMMAND_MASK;

    /**
     * The serial instance used to communicate with the Maestro.
     */
    private final Serial serial;

    /**
     * The Maestro's <a href="https://www.pololu.com/docs/0J40/5.a">device number</a>.
     */
    private final byte device;

    /**
     * Constructs a new {@code PololuMaestro} that reads and writes to given serial connection, with the
     * given <a href="https://www.pololu.com/docs/0J40/5.a">device number</a>.
     *
     * @param serial the open serial connection
     * @param device the Maestro's device number
     */
    public PololuMaestro(final Serial serial, final byte device) {
        this.serial = serial;
        this.device = device;
    }

    /**
     * Sets the target for the given channel.
     * <p>
     * If the channel is configured as a servo, then the target represents the pulse width to transmit in units of
     * quarter-microseconds. A target value of {@code 0} tells the Maestro to stop sending pulses to the servo. If the
     * channel is configured as a digital output, values less than {@code 6000} tell the Maestro to drive the line low,
     * while values of {@code 6000} or greater tell the Maestro to drive the line high.
     *
     * @param channel the channel
     * @param target the target
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final void setTarget(final byte channel, final short target) {
        final short microseconds = (short) (target * 4);
        final byte lsb = (byte) (microseconds & 0x7F);
        final byte msb = (byte) ((microseconds >> 7) & 0x7F);

        serial.write(new byte[]{START_BYTE, device, COMMAND_SET_TARGET, channel, lsb, msb});
    }

    /**
     * Returns the errors that the Maestro has detected.
     * <p>
     * See <a href="https://www.pololu.com/docs/0J40/4.b">Section 4.b</a> for the list of the specific errors that
     * can be detected by the Maestro.
     * @return the errors that the Maestro has detected
     */
    public final BitSet getErrors() {
        serial.write(new byte[]{START_BYTE, device, COMMAND_GET_ERRORS});
        final ByteBuffer response = ByteBuffer.allocate(2).putChar(serial.read());
        return BitSet.valueOf(new byte[]{response.get(1), response.get(0)});
    }

    /**
     * Returns the position value of the given channel.
     * @param channel the channel number
     * @return the position value of the given channel
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final short getPosition(final byte channel) {
        serial.write(new byte[]{START_BYTE, device, COMMAND_GET_POSITION, channel});
        final ByteBuffer response = ByteBuffer.allocate(4).putChar(serial.read()).putChar(serial.read());
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).put(
            new byte[]{response.get(1), response.get(3)}).getShort(0);
    }
}
