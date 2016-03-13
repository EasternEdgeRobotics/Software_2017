package com.easternedgerobotics.rov.value;

class Cpu implements ImmutableValueCompanion<CpuValue> {
    /**
     * The ARM clock frequency.
     */
    public long frequency;

    /**
     * The core temperature of the main SoC in celsius.
     */
    public float temperature;

    /**
     * The core voltage.
     */
    public float voltage;

    @Override
    public CpuValue asImmutable() {
        return new CpuValue(this);
    }
}
