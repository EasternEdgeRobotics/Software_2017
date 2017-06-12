package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.MockRovConfig;
import com.easternedgerobotics.rov.config.RovConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.Accelerometer;
import com.easternedgerobotics.rov.io.Barometer;
import com.easternedgerobotics.rov.io.Bluetooth;
import com.easternedgerobotics.rov.io.Gyroscope;
import com.easternedgerobotics.rov.io.Magnetometer;
import com.easternedgerobotics.rov.io.PWM;
import com.easternedgerobotics.rov.io.Thermometer;
import com.easternedgerobotics.rov.test.OrgPwmTinylogSuppressionRule;
import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AftPowerValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.ForePowerValue;
import com.easternedgerobotics.rov.value.GlobalPowerValue;
import com.easternedgerobotics.rov.value.HeavePowerValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.LightASpeedValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.PitchPowerValue;
import com.easternedgerobotics.rov.value.RollPowerValue;
import com.easternedgerobotics.rov.value.RotationValue;
import com.easternedgerobotics.rov.value.SurgePowerValue;
import com.easternedgerobotics.rov.value.SwayPowerValue;
import com.easternedgerobotics.rov.value.ToolingASpeedValue;
import com.easternedgerobotics.rov.value.TopsideHeartbeatValue;
import com.easternedgerobotics.rov.value.YawPowerValue;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
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

class MockBluetooth implements Bluetooth {
    public void start(final EventPublisher eventPublisher) { }

    public void stop() { }
}

@SuppressWarnings({"checkstyle:magicnumber"})
public class RovTest {
    private static final RovConfig ROV_CONFIG = new MockRovConfig();

    @Rule
    public final TestRule rovLoggerRule = new OrgPwmTinylogSuppressionRule(Rov.class);

    @Test
    public final void doesInitializeThrustersWithZero() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);

        Mockito.verify(maestro.get(ROV_CONFIG.starboardForeChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertForeChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.starboardAftChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portForeChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertAftChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portAftChannel())).writeZero();
    }

    @Test
    public final void doesSoftShutdownAfterFalseHeartbeatFromTopside() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        eventPublisher.emit(new TopsideHeartbeatValue(false));
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.starboardForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.starboardAftChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertAftChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portAftChannel()), Mockito.times(2)).writeZero();
    }

    @Test
    public final void doesZeroOutThrustersAfterTimeout() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        scheduler.advanceTimeBy(ROV_CONFIG.maxHeartbeatGap(), TimeUnit.SECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.starboardForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.starboardAftChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertAftChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portAftChannel()), Mockito.times(2)).writeZero();
    }

    @Test
    public final void writesValueOfZeroToThrustersWithoutInputMotion() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        eventPublisher.emit(new TopsideHeartbeatValue(false));
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.starboardForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.starboardAftChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portForeChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertAftChannel()), Mockito.times(2)).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portAftChannel()), Mockito.times(2)).writeZero();
    }

    @Test
    public final void doesWriteMotionValueAndPowerInputToThrusters() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        eventPublisher.emit(new TopsideHeartbeatValue(true));
        eventPublisher.emit(new GlobalPowerValue(1));
        eventPublisher.emit(new HeavePowerValue(1));
        eventPublisher.emit(new SwayPowerValue(1));
        eventPublisher.emit(new SurgePowerValue(1));
        eventPublisher.emit(new PitchPowerValue(1));
        eventPublisher.emit(new YawPowerValue(1));
        eventPublisher.emit(new RollPowerValue(1));
        eventPublisher.emit(new AftPowerValue(1));
        eventPublisher.emit(new ForePowerValue(1));
        eventPublisher.emit(new MotionValue(0, 0, 1, 0, 0, 0));
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.starboardForeChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertForeChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.starboardAftChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portForeChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.vertAftChannel())).writeZero();
        Mockito.verify(maestro.get(ROV_CONFIG.portAftChannel())).writeZero();

        Mockito.verify(maestro.get(ROV_CONFIG.starboardForeChannel())).write(0);
        Mockito.verify(maestro.get(ROV_CONFIG.starboardForeChannel())).write(
            MockitoHamcrest.floatThat(CoreMatchers.not(0f)));

        Mockito.verify(maestro.get(ROV_CONFIG.vertForeChannel()), Mockito.times(2)).write(0);

        Mockito.verify(maestro.get(ROV_CONFIG.starboardAftChannel())).write(0);
        Mockito.verify(maestro.get(ROV_CONFIG.starboardAftChannel())).write(
            MockitoHamcrest.floatThat(CoreMatchers.not(0f)));

        Mockito.verify(maestro.get(ROV_CONFIG.portForeChannel())).write(0);
        Mockito.verify(maestro.get(ROV_CONFIG.portForeChannel())).write(
            MockitoHamcrest.floatThat(CoreMatchers.not(0f)));

        Mockito.verify(maestro.get(ROV_CONFIG.vertAftChannel()), Mockito.times(2)).write(0);

        Mockito.verify(maestro.get(ROV_CONFIG.portAftChannel())).write(0);
        Mockito.verify(maestro.get(ROV_CONFIG.portAftChannel())).write(
            MockitoHamcrest.floatThat(CoreMatchers.not(0f)));
    }

    @Test
    public final void doesUpdateCameraAGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        eventPublisher.emit(new TopsideHeartbeatValue(true));
        eventPublisher.emit(new CameraSpeedValueA(1));
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.cameraAMotorChannel())).write(1);
    }

    @Test
    public final void doesUpdateCameraBGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        eventPublisher.emit(new TopsideHeartbeatValue(true));
        eventPublisher.emit(new CameraSpeedValueB(1));
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.cameraBMotorChannel())).write(1);
    }

    @Test
    public final void doesUpdateToolingGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        eventPublisher.emit(new TopsideHeartbeatValue(true));
        eventPublisher.emit(new ToolingASpeedValue(1));
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.toolingAMotorChannel())).write(1);
    }

    @Test
    public final void doesUpdateLightGivenInput() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockBluetooth bluetooth = new MockBluetooth();
        final Rov rov = new Rov(eventPublisher, maestro, imu, bluetooth, ROV_CONFIG);

        rov.init(scheduler, scheduler);
        eventPublisher.emit(new TopsideHeartbeatValue(true));
        eventPublisher.emit(new LightASpeedValue(1));
        scheduler.advanceTimeBy(ROV_CONFIG.sleepDuration(), TimeUnit.MILLISECONDS);

        Mockito.verify(maestro.get(ROV_CONFIG.lightAChannel())).write(1);
    }
}
