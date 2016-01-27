package com.easternedgerobotics.rov.control;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.easternedgerobotics.rov.value.MotionValue;

/* ***
 * Tests for thruster control. Inputs motion values (like joystick input) and looks for the correct changes to thruster values.
 * Since it only observes thruster values, these tests don't go into the i2c interaction.
 */
public class ThrusterTest {
	
	private ThrusterTestModel model;
	private I2CSim portFore;
	private I2CSim portAft;
	private I2CSim portVert;
	private I2CSim stbdFore;
	private I2CSim stbdAft;
	private I2CSim stbdVert;
	
	@Before
	public void setup(){
		model = new ThrusterTestModel("localhost", 8000);
		portFore = model.getI2CSim(ThrusterControlTestModel.PORT_FORE_NAME);
		portAft = model.getI2CSim(ThrusterControlTestModel.PORT_AFT_NAME);
		portVert = model.getI2CSim(ThrusterControlTestModel.PORT_VERT_NAME);
		stbdFore = model.getI2CSim(ThrusterControlTestModel.STARBOARD_FORE_NAME);
		stbdAft = model.getI2CSim(ThrusterControlTestModel.STARBOARD_AFT_NAME);
		stbdVert = model.getI2CSim(ThrusterControlTestModel.STARBOARD_VERT_NAME);
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
		Assert.assertEquals(-128.0, portFore.getLastWrite()[0], 0.0001);
		Assert.assertEquals(1.0, portFore.getLastWrite()[1], 0.0001);
		Assert.assertEquals(127.0, portAft.getLastWrite()[0], 0.0001);
		Assert.assertEquals(-1.0, portAft.getLastWrite()[1], 0.0001);
		Assert.assertEquals(0.0, portVert.getLastWrite()[0], 0.0001);
		Assert.assertEquals(0.0, portVert.getLastWrite()[1], 0.0001);
		Assert.assertEquals(127.0, stbdFore.getLastWrite()[0], 0.0001);
		Assert.assertEquals(-1.0, stbdFore.getLastWrite()[1], 0.0001);
		Assert.assertEquals(-128.0, stbdAft.getLastWrite()[0], 0.0001);
		Assert.assertEquals(1.0, stbdAft.getLastWrite()[1], 0.0001);
		Assert.assertEquals(0.0, stbdVert.getLastWrite()[0], 0.0001);
		Assert.assertEquals(0.0, stbdVert.getLastWrite()[1], 0.0001);
	}
}
