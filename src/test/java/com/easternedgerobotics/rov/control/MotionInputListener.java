package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.MotionValue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

/**
 * Publishes motion values based on user input in the Thruster Control Test View.
 */
public class MotionInputListener implements ActionListener {

    JTextField surge;

    JTextField sway;

    JTextField heave;

    JTextField yaw;

    JTextField roll;

    private TestView view;

    private EventPublisher eventPublisher;

    public MotionInputListener(
        final JTextField surgeField,
        final JTextField swayField,
        final JTextField heaveField,
        final JTextField yawField,
        final JTextField rollField,
        final TestModel model,
        final TestView testView
    ) {
        this.surge = surgeField;
        this.sway = swayField;
        this.heave = heaveField;
        this.yaw = yawField;
        this.roll = rollField;
        this.view = testView;
        this.eventPublisher = model.getEventPublisher();
    }

    public final void actionPerformed(final ActionEvent e) {
        final float surgeValue = Float.parseFloat(surge.getText());
        final float swayValue = Float.parseFloat(sway.getText());
        final float heaveValue = Float.parseFloat(heave.getText());
        final float yawValue = Float.parseFloat(yaw.getText());
        final float rollValue = Float.parseFloat(roll.getText());
        eventPublisher.emit(MotionValue.create(heaveValue, swayValue, surgeValue, 0, yawValue, rollValue));
        eventPublisher.emit(HeartbeatValue.create(true));
        view.refresh();
    }
}
