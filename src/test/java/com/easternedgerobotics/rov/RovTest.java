package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.Accelerometer;
import com.easternedgerobotics.rov.io.Barometer;
import com.easternedgerobotics.rov.io.Gyroscope;
import com.easternedgerobotics.rov.io.Magnetometer;
import com.easternedgerobotics.rov.io.PWM;
import com.easternedgerobotics.rov.io.Thermometer;
import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.LightSpeedValue;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.RotationValue;
import com.easternedgerobotics.rov.value.ToolingSpeedValue;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;
import rx.schedulers.TestScheduler;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

interface Channel extends ADC, PWM {
    // ???
}

class MockMaestro extends AbstractList<Channel> {
    private static final byte NUMBER_OF_CHANNELS = 24;

    private final Map<Byte, Channel> channels = new HashMap<>();

    @Override
    public final Channel get(final int index) {
        return channels.computeIfAbsent((byte) index, k -> Mockito.mock(Channel.class, Mockito.RETURNS_SELF));
    }

    @Override
    public final int size() {
        return NUMBER_OF_CHANNELS;
    }
}

class MockAltIMU implements Accelerometer, Barometer, Thermometer, Gyroscope, Magnetometer {
    private AccelerationValue acceleration = new AccelerationValue();

    private InternalPressureValue pressure = new InternalPressureValue();

    private AngularVelocityValue angularVelocity = new AngularVelocityValue();

    private RotationValue rotation = new RotationValue();

    private InternalTemperatureValue temperature = new InternalTemperatureValue();

    public void setAcceleration(final AccelerationValue acceleration) {
        this.acceleration = acceleration;
    }

    public void setPressure(final InternalPressureValue pressure) {
        this.pressure = pressure;
    }

    public void setAngularVelocity(final AngularVelocityValue angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public void setRotation(final RotationValue rotation) {
        this.rotation = rotation;
    }

    public void setTemperature(final InternalTemperatureValue temperature) {
        this.temperature = temperature;
    }

    @Override
    public AccelerationValue acceleration() {
        return acceleration;
    }

    @Override
    public InternalPressureValue pressure() {
        return pressure;
    }

    @Override
    public AngularVelocityValue angularVelocity() {
        return angularVelocity;
    }

    @Override
    public RotationValue rotation() {
        return rotation;
    }

    @Override
    public InternalTemperatureValue temperature() {
        return temperature;
    }
}

@SuppressWarnings({"checkstyle:magicnumber"})
public class RovTest {
    @Test
    public final void doesInitializeThrustersWithZero() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);

        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_VERT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_VERT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL)).writeZero();
    }

    @Test
    public final void doesSoftShutdownAfterFalseHeartbeatFromTopside() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        eventPublisher.testObserver(HeartbeatValue.class).onNext(new HeartbeatValue(false));
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_VERT_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_VERT_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL), Mockito.times(2)).writeZero();
    }

    @Test
    public final void doesZeroOutThrustersAfterTimeout() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        scheduler.advanceTimeBy(Rov.MAX_HEARTBEAT_GAP, TimeUnit.SECONDS);

        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_VERT_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_VERT_CHANNEL), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL), Mockito.times(2)).writeZero();
    }

    @Test
    public final void writesValueOfZeroToThrustersWithoutInputMotion() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        eventPublisher.testObserver(HeartbeatValue.class).onNext(new HeartbeatValue(true));
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_VERT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_VERT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL)).writeZero();

        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.STARBOARD_VERT_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.PORT_VERT_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL)).write(0);
    }

    @Test
    public final void doesWriteMotionValueAndPowerInputToThrusters() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        eventPublisher.testObserver(HeartbeatValue.class).onNext(new HeartbeatValue(true));
        eventPublisher.testObserver(MotionPowerValue.class).onNext(new MotionPowerValue(1, 1, 1, 1, 1, 1, 1));
        eventPublisher.testObserver(MotionValue.class).onNext(new MotionValue(0, 0, 1, 0, 0, 0));
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_VERT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_VERT_CHANNEL)).writeZero();
        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL)).writeZero();

        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.STARBOARD_FORE_CHANNEL)).write(MockitoHamcrest.floatThat(CoreMatchers.not(0f)));

        Mockito.verify(maestro.get(Rov.STARBOARD_VERT_CHANNEL), Mockito.times(2)).write(0);

        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.STARBOARD_AFT_CHANNEL)).write(MockitoHamcrest.floatThat(CoreMatchers.not(0f)));

        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.PORT_FORE_CHANNEL)).write(MockitoHamcrest.floatThat(CoreMatchers.not(0f)));

        Mockito.verify(maestro.get(Rov.PORT_VERT_CHANNEL), Mockito.times(2)).write(0);

        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL)).write(0);
        Mockito.verify(maestro.get(Rov.PORT_AFT_CHANNEL)).write(MockitoHamcrest.floatThat(CoreMatchers.not(0f)));
    }

    @Test
    public final void doesUpdateCameraAGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        eventPublisher.testObserver(HeartbeatValue.class).onNext(new HeartbeatValue(true));
        eventPublisher.testObserver(CameraSpeedValueA.class).onNext(new CameraSpeedValueA(1));
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(Rov.CAMERA_A_MOTOR_CHANNEL)).write(1);
    }

    @Test
    public final void doesUpdateCameraBGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        eventPublisher.testObserver(HeartbeatValue.class).onNext(new HeartbeatValue(true));
        eventPublisher.testObserver(CameraSpeedValueB.class).onNext(new CameraSpeedValueB(1));
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(Rov.CAMERA_B_MOTOR_CHANNEL)).write(1);
    }

    @Test
    public final void doesUpdateToolingGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        eventPublisher.testObserver(HeartbeatValue.class).onNext(new HeartbeatValue(true));
        eventPublisher.testObserver(ToolingSpeedValue.class).onNext(new ToolingSpeedValue(1));
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(Rov.TOOLING_MOTOR_CHANNEL)).write(1);
    }

    @Test
    public final void doesUpdateLightGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final Rov rov = new Rov(eventPublisher, maestro, imu);

        rov.init(scheduler, scheduler);
        eventPublisher.testObserver(HeartbeatValue.class).onNext(new HeartbeatValue(true));
        eventPublisher.testObserver(LightSpeedValue.class).onNext(new LightSpeedValue(1));
        scheduler.advanceTimeBy(Rov.SLEEP_DURATION, TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(Rov.LIGHT_CHANNEL)).write(1);
    }
}
