package com.easternedgerobotics.rov.fx;

import javax.inject.Inject;

public class CpuInformationViewController implements ViewController {
    private final CpuInformationView view;

    @Inject
    public CpuInformationViewController(final CpuInformationView view) {
        this.view = view;
    }
}
