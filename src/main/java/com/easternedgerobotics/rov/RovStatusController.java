package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.value.HeartbeatValue;

import rx.Observer;

final class RovStatusController implements Observer<HeartbeatValue> {
    private final Rov rov;

    public RovStatusController(final Rov rov) {
        this.rov = rov;
    }

    @Override
    public void onNext(final HeartbeatValue heartbeat) {

    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(final Throwable error) {
        rov.shutdown();
    }
}
