package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;

public class PressureSensorViewController implements ViewController {
    private final EventPublisher eventPublisher;

    private final PressureSensorView view;

    public PressureSensorViewController(final EventPublisher eventPublisher, final PressureSensorView view) {
        this.eventPublisher = eventPublisher;
        this.view = view;
    }
}
