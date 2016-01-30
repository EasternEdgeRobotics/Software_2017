package com.easternedgerobotics.rov.value;

class JoystickAxes implements ImmutableValueCompanion<JoystickAxesValue> {
    public float[] axes;

    @Override
    public JoystickAxesValue asImmutable() {
        return new JoystickAxesValue(this);
    }
}
