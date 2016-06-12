package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.CurrentValue;

import java.util.function.Function;

public final class CurrentSensor {
    @SuppressWarnings("checkstyle:MagicNumber")
    public static final Function<ADC, CurrentSensor> V05 = adc -> new CurrentSensor(adc,  5);

    @SuppressWarnings("checkstyle:MagicNumber")
    public static final Function<ADC, CurrentSensor> V12 = adc -> new CurrentSensor(adc, 12);

    @SuppressWarnings("checkstyle:MagicNumber")
    public static final Function<ADC, CurrentSensor> V48 = adc -> new CurrentSensor(adc, 48);

    private final ADC device;

    private final float volts;

    private CurrentSensor(final ADC device, final float volts) {
        this.device = device;
        this.volts = volts;
    }

    /**
     * Returns the current value read from the ADC.
     * @return the current value read from the ADC
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public CurrentValue read() {
        return new CurrentValue(volts, (float) ((device.voltage() - 0.5) / 0.133));
    }
}
