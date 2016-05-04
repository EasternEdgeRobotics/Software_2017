package com.easternedgerobotics.rov.value;

import java.util.Objects;

public class CpuValue implements MutableValueCompanion<Cpu> {
    /**
     * Constructs a CPU value with the given frequency, temperature, and voltage.
     *
     * @param frequency the ARM clock frequency.
     * @param temperature The core temperature of the main SoC in celsius.
     * @param voltage the core voltage.
     * @return a CPU value with the given frequency, temperature, and voltage.
     */
    public static CpuValue create(final long frequency, final float temperature, final float voltage) {
        final Cpu cpu = new Cpu();
        cpu.frequency = frequency;
        cpu.temperature = temperature;
        cpu.voltage = voltage;
        return new CpuValue(cpu);
    }

    private final Cpu cpu;

    CpuValue(final Cpu cpu) {
        this.cpu = cpu;
    }

    public final long getFrequency() {
        return cpu.frequency;
    }

    public final float getTemperature() {
        return cpu.temperature;
    }

    public final float getVoltage() {
        return cpu.voltage;
    }

    @Override
    public final Cpu asMutable() {
        return cpu;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CpuValue cpuValue = (CpuValue) o;
        return Objects.equals(cpu, cpuValue.cpu);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(cpu);
    }

    @Override
    public final String toString() {
        return String.format(
            "CpuValue{frequency=%d, temperature=%f, voltage=%f}",
            cpu.frequency,
            cpu.temperature,
            cpu.voltage
        );
    }
}
