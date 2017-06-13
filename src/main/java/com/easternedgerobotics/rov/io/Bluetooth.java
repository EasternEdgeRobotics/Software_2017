package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.event.EventPublisher;

public interface Bluetooth {
    void start(EventPublisher eventPublisher);

    void stop();
}
