package com.easternedgerobotics.rov.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.event.EventPublisher;

/* ******
 * Publishes motion values based on user input in the Thruster Control Test View. 
 */
public class MotionInputListener implements ActionListener {

	JTextField surge;
	JTextField sway;
	JTextField heave;
	JTextField yaw;
	JTextField roll;
	private ThrusterControlTestView view;
	private EventPublisher eventPublisher;
	
	public MotionInputListener(JTextField surge, JTextField sway, JTextField heave, JTextField yaw,
						 JTextField roll, ThrusterControlTestModel model, ThrusterControlTestView view){
		this.surge = surge;
		this.sway = sway;
		this.heave = heave;
		this.yaw = yaw;
		this.roll = roll;
		this.view = view;
		this.eventPublisher = model.getEventPublisher();
	}

	public void actionPerformed(ActionEvent e) {
		float surgeValue = Float.parseFloat(surge.getText());
		float swayValue = Float.parseFloat(sway.getText());
		float heaveValue = Float.parseFloat(heave.getText());
		float yawValue = Float.parseFloat(yaw.getText());
		float rollValue = Float.parseFloat(roll.getText());
		eventPublisher.emit(MotionValue.create(heaveValue, swayValue, surgeValue, 0, yawValue, rollValue));
		eventPublisher.emit(HeartbeatValue.create(true));
		view.refresh();
	}

}
