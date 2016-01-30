package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.HeartbeatValue;

import rx.Observable;

final class HeartbeatController {
    private final EventPublisher eventPublisher;

    private final Observable<?> timer;

    public HeartbeatController(final EventPublisher eventPublisher, final Observable<?> timer) {
        this.eventPublisher = eventPublisher;
        this.timer = timer;
    }

    public void start() {
        timer.subscribe(x -> beat());
    }

    private void beat() {
        eventPublisher.emit(HeartbeatValue.create(true));
    }
}
