package com.easternedgerobotics.rov.control;

//import com.easternedgerobotics.rov.value.MotionValue;

//import org.junit.Assert;
import org.junit.Before;
//import org.junit.Test;

/* ***
 * Tests for thruster control. Inputs motion values (like joystick input) and looks for
 * the correct changes to thruster values.
 * Since it only observes thruster values, these tests don't go into the i2c interaction.
 */
public class ThrusterControlTest {
    
    private final int port = 8001;
    
    private final double precision = 0.0001;
    
    private final int firstSleepDuration = 3000;
    
    private final int secondSleepDuration = 1000;
    
    private ThrusterControlTestModel model;
    
    private ThrusterValueListener portFore;
    
    private ThrusterValueListener portAft;
    
    private ThrusterValueListener portVert;
    
    private ThrusterValueListener stbdFore;
    
    private ThrusterValueListener stbdAft;
    
    private ThrusterValueListener stbdVert;
    
    @Before
    public final void setup() {
        model = new ThrusterControlTestModel("localhost", port);
        portFore = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_FORE_NAME);
        portAft = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_AFT_NAME);
        portVert = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_VERT_NAME);
        stbdFore = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_FORE_NAME);
        stbdAft = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_AFT_NAME);
        stbdVert = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_VERT_NAME);
        model.update();
    }
    
//    @Test
//    public final void testSurge() {
//        model.getEventPublisher().emit(MotionValue.create(0, 0, 1, 0, 0, 0));
//        try {
//            java.lang.Thread.sleep(firstSleepDuration);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        model.update();
//        try {
//            java.lang.Thread.sleep(secondSleepDuration);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals(-1.0, portFore.getOutput(), precision);
//        Assert.assertEquals(1.0, portAft.getOutput(), precision);
//        Assert.assertEquals(0.0, portVert.getOutput(), precision);
//        Assert.assertEquals(1.0, stbdFore.getOutput(), precision);
//        Assert.assertEquals(-1.0, stbdAft.getOutput(), precision);
//        Assert.assertEquals(0.0, stbdVert.getOutput(), precision);
//    }
}
