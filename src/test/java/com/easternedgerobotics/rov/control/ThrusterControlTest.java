package com.easternedgerobotics.rov.control;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.easternedgerobotics.rov.value.MotionValue;

/* ***
 * Tests for thruster control. Inputs motion values (like joystick input) and looks for the correct changes to thruster values.
 * Since it only observes thruster values, these tests don't go into the i2c interaction.
 */
public class ThrusterControlTest {
	
	private ThrusterControlTestModel model;
	private ThrusterValueListener portFore;
	private ThrusterValueListener portAft;
	private ThrusterValueListener portVert;
	private ThrusterValueListener stbdFore;
	private ThrusterValueListener stbdAft;
	private ThrusterValueListener stbdVert;
	
	@Before
	public void setup(){
		model = new ThrusterControlTestModel("localhost", 8000);
		portFore = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_FORE_NAME);
		portAft = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_AFT_NAME);
		portVert = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_VERT_NAME);
		stbdFore = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_FORE_NAME);
		stbdAft = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_AFT_NAME);
		stbdVert = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_VERT_NAME);
		model.update();
	}
	
	@Test
	public void testSurge(){
		model.getEventPublisher().emit(MotionValue.create(0, 0, 1, 0, 0, 0));
		try{
			java.lang.Thread.sleep(3000);
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		model.update();
		try{
			java.lang.Thread.sleep(1000);
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		Assert.assertEquals(-1.0, portFore.getOutput(), 0.0001);
		Assert.assertEquals(1.0, portAft.getOutput(), 0.0001);
		Assert.assertEquals(0.0, portVert.getOutput(), 0.0001);
		Assert.assertEquals(1.0, stbdFore.getOutput(), 0.0001);
		Assert.assertEquals(-1.0, stbdAft.getOutput(), 0.0001);
		Assert.assertEquals(0.0, stbdVert.getOutput(), 0.0001);
	}
}
