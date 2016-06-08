package com.easternedgerobotics.rov.fx;

import javafx.scene.control.Separator;

import javax.inject.Inject;

public class MovementViewController implements ViewController {
    final MovementView movementView;

    final ThrusterPowerSlidersView thrusterPowerSlidersView;

    final PrecisionPowerEnablesView precisionPowerEnablesView;

    @Inject
    public MovementViewController(
        final MovementView movementView,
        final ThrusterPowerSlidersView thrusterPowerSlidersView,
        final PrecisionPowerEnablesView precisionPowerEnablesView
    ) {
        this.movementView = movementView;
        this.thrusterPowerSlidersView = thrusterPowerSlidersView;
        this.precisionPowerEnablesView = precisionPowerEnablesView;
    }

    @Override
    public final void onCreate() {
        movementView.vbox.getChildren().addAll(
            thrusterPowerSlidersView.getParent(),
            new Separator(),
            precisionPowerEnablesView.getParent()
        );
    }
}
