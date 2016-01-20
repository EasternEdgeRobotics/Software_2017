package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.ThrusterValue;

/* *****
 * Subscribes to updates to the speed of a specific thruster and stores it.
 */
public class ThrusterValueListener {
	
	private float output;
	
	public ThrusterValueListener(ThrusterControlTestModel model, String thrusterName){
		model.getEventPublisher().valuesOfType(ThrusterValue.class).subscribe(thrusterValue -> {
            if (thrusterValue.getName().equals(thrusterName)) {
                output = thrusterValue.getSpeed();
            }
		});
	}
	
	public float getOutput(){
		return output;
	}
}
