package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;

public interface TestModel {
    public EventPublisher getEventPublisher();

    public void update();

    public void updateZero();
}
