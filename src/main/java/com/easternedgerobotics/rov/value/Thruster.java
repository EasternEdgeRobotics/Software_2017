package com.easternedgerobotics.rov.value;

/**
 * Created by cal on 10/16/2015.
 */
public class Thruster implements ImmutableValueCompanion<ThrusterValue> {
    public String name;
    public float speed;
    
    public float voltage;
    public float current;
    public float temperature;


    @Override
    public ThrusterValue asImmutable() {
        return new ThrusterValue(this);
    }
}
