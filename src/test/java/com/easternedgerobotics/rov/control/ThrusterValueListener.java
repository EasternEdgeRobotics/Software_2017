package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.ThrusterValue;

/**
 * Subscribes to updates to the speed of a specific thruster and stores it.
 */
public class ThrusterValueListener {
    private float output;

    public ThrusterValueListener(final ThrusterControlTestModel model, final String thrusterName) {
        model.getEventPublisher().valuesOfType(ThrusterValue.class).subscribe(thrusterValue -> {
            if (thrusterValue.getName().equals(thrusterName)) {
                output = thrusterValue.getSpeed();
            }
        });
    }

    public final float getOutput() {
        return output;
    }
}
