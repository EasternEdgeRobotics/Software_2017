package com.easternedgerobotics.rov.io.devices;

public interface Light {
    void write(boolean active);

    void flash();
}
