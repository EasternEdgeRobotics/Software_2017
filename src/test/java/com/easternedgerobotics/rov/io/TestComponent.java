package com.easternedgerobotics.rov.io;

import net.java.games.input.Component;

abstract class TestComponent implements Component {
    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public boolean isAnalog() {
        return false;
    }

    @Override
    public float getDeadZone() {
        return 0;
    }

    @Override
    public float getPollData() {
        return 0;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
