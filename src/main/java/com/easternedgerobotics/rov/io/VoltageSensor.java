package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.devices.ADC;
import com.easternedgerobotics.rov.value.VoltageValue;

import java.util.function.DoubleFunction;
import java.util.function.Function;

public final class VoltageSensor {
    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
    public static final Function<ADC, VoltageSensor> V05 = adc -> new VoltageSensor(adc,  5, volts -> volts / 2.92 *  5);

    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
    public static final Function<ADC, VoltageSensor> V12 = adc -> new VoltageSensor(adc, 12, volts -> volts / 3.03 * 12);

    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
    public static final Function<ADC, VoltageSensor> V48 = adc -> new VoltageSensor(adc, 48, volts -> volts / 3.11 * 48);

    private final ADC device;

    private final float volts;

    private final DoubleFunction<Double> conversion;

    private VoltageSensor(final ADC device, final float volts, final DoubleFunction<Double> conversion) {
        this.device = device;
        this.volts = volts;
        this.conversion = conversion;
    }

    /**
     * Returns the converted voltage read from the ADC.
     * @return the converted voltage read from the ADC
     */
    public VoltageValue read() {
        return new VoltageValue(volts, conversion.apply(device.voltage()).floatValue());
    }
}
