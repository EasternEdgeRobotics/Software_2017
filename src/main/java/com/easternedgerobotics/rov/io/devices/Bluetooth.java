package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.event.EventPublisher;

public interface Bluetooth {
    void start(EventPublisher eventPublisher);

    void stop();
}
