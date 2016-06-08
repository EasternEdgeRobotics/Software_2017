package com.easternedgerobotics.rov.fx;

import javax.inject.Inject;

public class EnableViewController implements ViewController {
    final EnableView view;

    @Inject
    public EnableViewController(final EnableView view) {
        this.view = view;
    }

    @Override
    public final void onCreate() { }
}
