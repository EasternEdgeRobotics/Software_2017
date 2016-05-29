package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.PWM;
import com.easternedgerobotics.rov.math.Range;

import com.pi4j.io.serial.Serial;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.Objects;
import java.util.function.DoubleFunction;

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

    private final Range input;

    private DoubleFunction<Double> rangeMap;

    MaestroChannel(final Serial serial, final byte maestro, final byte channel) {
        this.serial = serial;
        this.maestro = maestro;
        this.channel = channel;
        this.input = new Range(-1, 1);
        this.rangeMap = Range.map(input, new Range(-1, 1));
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

    /**
     * Write a value to this channel.
     *
     * @param value percent from -1 to 1
     */
    @Override
    public final void write(final float value) {
        setTarget((short) Math.round(rangeMap.apply(value)));
    }

    /**
     * Write zero to the channel.
     */
    @Override
    public final void writeZero() {
        setTarget((short) Math.round(rangeMap.apply(0)));
    }

    /**
     * Sets the output signal range.
     *
     * @param range the output signal range
     * @return the PWM instance
     */
    @Override
    public final PWM setOutputRange(final Range range) {
        rangeMap = Range.map(input, range);
        return this;
    }

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final MaestroChannel that = (MaestroChannel) other;
        return maestro == that.maestro && channel == that.channel;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(maestro, channel);
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
}
