package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.devices.Light;
import com.easternedgerobotics.rov.value.LightValue;

import rx.Observable;

public class DigitalLight {
    /**
     * The raspi gpio pin being used.
     */
    private final Light light;

    /**
     * The latest value from the speed observable.
     */
    private LightValue value;

    public DigitalLight(final Observable<LightValue> values, final Light light) {
        this.light = light;
        values.subscribe(v -> value = v);
    }

    public final void write() {
        light.write(value.getActive());
    }

    public final void writeZero() {
        light.write(false);
    }

    public final void flash() {
        light.flash();
    }
}
