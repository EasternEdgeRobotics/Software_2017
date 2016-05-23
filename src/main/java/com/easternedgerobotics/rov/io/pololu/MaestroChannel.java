package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.PWM;

import com.pi4j.io.serial.Serial;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

final class MaestroChannel implements ADC, PWM {
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

    private final Serial serial;

    private final byte maestro;

    private final byte channel;

    private float maxForward;

    private float maxReverse;

    MaestroChannel(final Serial serial, final byte maestro, final byte channel) {
        this.serial = serial;
        this.maestro = maestro;
        this.channel = channel;
        this.maxForward = +1;
        this.maxReverse = -1;
    }

    /**
     * Returns the voltage of this channel.
     * @return the voltage of this channel
     */
    @Override
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final float voltage() {
        return 5 * (getPosition() / 1023.0f);
    }

    @Override
    public final PWM setMaxForward(final float maxForward) {
        this.maxForward = maxForward;
        return this;
    }

    @Override
    public final PWM setMaxReverse(final float maxReverse) {
        this.maxReverse = maxReverse;
        return this;
    }

    /**
     * Write a value to this channel.
     *
     * @param value percent from -1 to 1
     */
    @Override
    public final void write(final float value) {
        setTarget(rangeMap(value));
    }

    /**
     * Write zero to the channel.
     */
    @Override
    public final void writeZero() {
        setTarget(rangeMap(0));
    }

    /**
     * Sets the target for the this channel.
     * <p>
     * If this channel is configured as a servo, then the target represents the pulse width to transmit in units of
     * quarter-microseconds. A target value of {@code 0} tells the Maestro to stop sending pulses to the servo. If the
     * channel is configured as a digital output, values less than {@code 6000} tell the Maestro to drive the line low,
     * while values of {@code 6000} or greater tell the Maestro to drive the line high.
     *
     * @param target the target
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    private void setTarget(final short target) {
        final short microseconds = (short) (target * 4);
        final byte lsb = (byte) (microseconds & 0x7F);
        final byte msb = (byte) ((microseconds >> 7) & 0x7F);

        serial.write(new byte[] {START_BYTE, maestro, COMMAND_SET_TARGET, channel, lsb, msb});
    }

    /**
     * Returns the position value of this channel.
     * @return the position value of this channel
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    private short getPosition() {
        serial.write(new byte[] {START_BYTE, maestro, COMMAND_GET_POSITION, channel});
        final ByteBuffer response = ByteBuffer.allocate(4).putChar(serial.read()).putChar(serial.read());
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).put(
            new byte[]{response.get(1), response.get(3)}).getShort(0);
    }

    /**
     * Returns the errors that the Maestro has detected.
     * <p>
     * See <a href="https://www.pololu.com/docs/0J40/4.b">Section 4.b</a> for the list of the specific errors that
     * can be detected by the Maestro.
     * @return the errors that the Maestro has detected
     */
    @SuppressWarnings("unused")
    private BitSet getErrors() {
        serial.write(new byte[] {START_BYTE, maestro, COMMAND_GET_ERRORS});
        final ByteBuffer response = ByteBuffer.allocate(2).putChar(serial.read());
        return BitSet.valueOf(new byte[]{response.get(1), response.get(0)});
    }

    /**
     * Returns the value mapped to the input microseconds valid for this PWM channel.
     *
     * @param value the output value from -1 to 1
     * @return the microseconds for pulse width signal
     */
    private short rangeMap(final float value) {
        return (short) Math.round(rangeMap(value, -1f, 1f, maxForward, maxReverse));
    }

    /**
     * Returns the given value mapped from Range A to Range B. See also
     * <a href="https://git.io/vwrID">{@code EasternEdge.Common.Utils.ExtensionMethods.NumberRangeMapExtensions}</a>.
     * @param value the value to map
     * @param fromA the start of the range the value is in now
     * @param fromB the end of the range the value is in now
     * @param toA the start of the range the value is being mapped to
     * @param toB the end of the range the value is being mapped to
     * @return the mapped value
     */
    private float rangeMap(final float value, final float fromA, final float fromB, final float toA, final float toB) {
        return toA + (toB - toA) * (value - fromA) / (fromB - fromA);
    }
}
