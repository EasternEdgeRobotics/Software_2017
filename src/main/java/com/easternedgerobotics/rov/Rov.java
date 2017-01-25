package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.SixThrusterConfig;
import com.easternedgerobotics.rov.control.TaskManager;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.Accelerometer;
import com.easternedgerobotics.rov.io.Barometer;
import com.easternedgerobotics.rov.io.CpuInformation;
import com.easternedgerobotics.rov.io.CurrentSensor;
import com.easternedgerobotics.rov.io.Gyroscope;
import com.easternedgerobotics.rov.io.Light;
import com.easternedgerobotics.rov.io.Magnetometer;
import com.easternedgerobotics.rov.io.Motor;
import com.easternedgerobotics.rov.io.PWM;
import com.easternedgerobotics.rov.io.Thermometer;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.io.VoltageSensor;
import com.easternedgerobotics.rov.io.pololu.AltIMU10v3;
import com.easternedgerobotics.rov.io.pololu.Maestro;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.LightSpeedValue;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.PortAftSpeedValue;
import com.easternedgerobotics.rov.value.PortForeSpeedValue;
import com.easternedgerobotics.rov.value.PortVertSpeedValue;
import com.easternedgerobotics.rov.value.StarboardAftSpeedValue;
import com.easternedgerobotics.rov.value.StarboardForeSpeedValue;
import com.easternedgerobotics.rov.value.StarboardVertSpeedValue;
import com.easternedgerobotics.rov.value.ToolingSpeedValue;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class Rov {
    static final long MAX_HEARTBEAT_GAP = 5;

    static final long SENSOR_POLL_INTERVAL = 10;

    static final long SLEEP_DURATION = 100;

    static final byte MAESTRO_DEVICE_NUMBER = 0x01;

    static final byte PORT_AFT_CHANNEL = 15;

    static final byte STARBOARD_AFT_CHANNEL = 12;

    static final byte PORT_FORE_CHANNEL = 16;

    static final byte STARBOARD_FORE_CHANNEL = 13;

    static final byte PORT_VERT_CHANNEL = 17;

    static final byte STARBOARD_VERT_CHANNEL = 14;

    static final byte CAMERA_A_MOTOR_CHANNEL = 18;

    static final byte CAMERA_B_MOTOR_CHANNEL = 19;

    static final byte TOOLING_MOTOR_CHANNEL = 22;

    static final byte LIGHT_CHANNEL = 23;

    static final byte VOLTAGE_SENSOR_05V_CHANNEL = 8;

    static final byte VOLTAGE_SENSOR_12V_CHANNEL = 7;

    static final byte VOLTAGE_SENSOR_48V_CHANNEL = 6;

    static final byte CURRENT_SENSOR_05V_CHANNEL = 11;

    static final byte CURRENT_SENSOR_12V_CHANNEL = 10;

    static final byte CURRENT_SENSOR_48V_CHANNEL = 9;

    static final int ALT_IMU_I2C_BUS = I2CBus.BUS_1;

    static final boolean ALT_IMU_SA0_HIGH = false;

    private final AtomicBoolean dead = new AtomicBoolean();

    private final Subject<Void, Void> killSwitch = PublishSubject.create();

    private final TaskManager unsafeTasks;

    private final TaskManager sensorTasks;

    <AltIMU extends Accelerometer & Barometer & Thermometer & Gyroscope & Magnetometer,
            MaestroChannel extends ADC & PWM> Rov(
        final Scheduler io, final Scheduler clock,
        final EventPublisher eventPublisher,
        final List<MaestroChannel> channels,
        final AltIMU imu
    ) {
        unsafeTasks = new TaskManager(SLEEP_DURATION, TimeUnit.MILLISECONDS, io);
        sensorTasks = new TaskManager(SENSOR_POLL_INTERVAL, TimeUnit.MILLISECONDS, io);

        Logger.debug("Wiring up heartbeat, timeout, and operation controller state");

        final Observable<HeartbeatValue> timeout = Observable.just(new HeartbeatValue(false))
            .delay(MAX_HEARTBEAT_GAP, TimeUnit.SECONDS, clock)
            .doOnNext(heartbeat -> Logger.warn("Timeout while waiting for heartbeat"))
            .concatWith(Observable.never());

        final Observable<HeartbeatValue> heartbeats = eventPublisher.valuesOfType(HeartbeatValue.class);

        Observable.interval(SLEEP_DURATION, TimeUnit.MILLISECONDS, clock).withLatestFrom(
            heartbeats.mergeWith(timeout.takeUntil(heartbeats).repeat()), (tick, heartbeat) -> heartbeat)
            .takeUntil(killSwitch)
            .observeOn(io)
            .subscribe(this::beat, Logger::error, () -> dead.set(true));

        Logger.debug("Wiring up operation critical items; Thrusters, Motors, Lights");

        final SixThrusterConfig thrusterConfig = new SixThrusterConfig();
        unsafeTasks
            .manage(eventPublisher.valuesOfType(MotionValue.class), new MotionValue(),
                eventPublisher.valuesOfType(MotionPowerValue.class), new MotionPowerValue(),
                (m, mp) -> Arrays.stream(thrusterConfig.update(m, mp)).forEach(eventPublisher::emit));

        final Range thrusterRange = new Range(Thruster.MAX_REV, Thruster.MAX_FWD);
        unsafeTasks
            .manage(eventPublisher.valuesOfType(PortAftSpeedValue.class), new PortAftSpeedValue(),
                new Thruster(channels.get(PORT_AFT_CHANNEL).setOutputRange(thrusterRange))::apply)
            .manage(eventPublisher.valuesOfType(StarboardAftSpeedValue.class), new StarboardAftSpeedValue(),
                new Thruster(channels.get(STARBOARD_AFT_CHANNEL).setOutputRange(thrusterRange))::apply)
            .manage(eventPublisher.valuesOfType(PortForeSpeedValue.class), new PortForeSpeedValue(),
                new Thruster(channels.get(PORT_FORE_CHANNEL).setOutputRange(thrusterRange))::apply)
            .manage(eventPublisher.valuesOfType(StarboardForeSpeedValue.class), new StarboardForeSpeedValue(),
                new Thruster(channels.get(STARBOARD_FORE_CHANNEL).setOutputRange(thrusterRange))::apply)
            .manage(eventPublisher.valuesOfType(PortVertSpeedValue.class), new PortVertSpeedValue(),
                new Thruster(channels.get(PORT_VERT_CHANNEL).setOutputRange(thrusterRange))::apply)
            .manage(eventPublisher.valuesOfType(StarboardVertSpeedValue.class), new StarboardVertSpeedValue(),
                new Thruster(channels.get(STARBOARD_VERT_CHANNEL).setOutputRange(thrusterRange))::apply);

        final Range motorRange = new Range(Motor.MAX_REV, Motor.MAX_FWD);
        unsafeTasks
            .manage(eventPublisher.valuesOfType(CameraSpeedValueA.class), new CameraSpeedValueA(),
                new Motor(channels.get(CAMERA_A_MOTOR_CHANNEL).setOutputRange(motorRange))::write)
            .manage(eventPublisher.valuesOfType(CameraSpeedValueB.class), new CameraSpeedValueB(),
                new Motor(channels.get(CAMERA_B_MOTOR_CHANNEL).setOutputRange(motorRange))::write)
            .manage(eventPublisher.valuesOfType(ToolingSpeedValue.class), new ToolingSpeedValue(),
                new Motor(channels.get(TOOLING_MOTOR_CHANNEL).setOutputRange(motorRange))::write);

        final Range lightRange = new Range(Light.MAX_REV, Light.MAX_FWD);
        unsafeTasks
            .manage(eventPublisher.valuesOfType(LightSpeedValue.class), new LightSpeedValue(),
                new Light(channels.get(LIGHT_CHANNEL).setOutputRange(lightRange))::write);

        unsafeTasks.stop();

        Logger.debug("Wiring up operation sensor items; Voltage, Current, IMU, CPU Info");

        sensorTasks
            .manage(VoltageSensor.V05.apply(channels.get(VOLTAGE_SENSOR_05V_CHANNEL))::read, eventPublisher::emit)
            .manage(VoltageSensor.V12.apply(channels.get(VOLTAGE_SENSOR_12V_CHANNEL))::read, eventPublisher::emit)
            .manage(VoltageSensor.V48.apply(channels.get(VOLTAGE_SENSOR_48V_CHANNEL))::read, eventPublisher::emit)
            .manage(CurrentSensor.V05.apply(channels.get(CURRENT_SENSOR_05V_CHANNEL))::read, eventPublisher::emit)
            .manage(CurrentSensor.V12.apply(channels.get(CURRENT_SENSOR_12V_CHANNEL))::read, eventPublisher::emit)
            .manage(CurrentSensor.V48.apply(channels.get(CURRENT_SENSOR_48V_CHANNEL))::read, eventPublisher::emit)
            .manage(() -> imu.pressure(), eventPublisher::emit)
            .manage(() -> imu.rotation(), eventPublisher::emit)
            .manage(() -> imu.acceleration(), eventPublisher::emit)
            .manage(() -> imu.angularVelocity(), eventPublisher::emit)
            .manage(() -> imu.temperature(), eventPublisher::emit)
            .manage(new CpuInformation()::pollCpu, eventPublisher::emit);

        sensorTasks.start();
    }

    private void beat(final HeartbeatValue heartbeat) {
        if (heartbeat.getOperational()) {
            unsafeTasks.start();
        } else {
            unsafeTasks.stop();
        }
    }

    void shutdown() {
        Logger.info("Shutting down");
        killSwitch.onCompleted();
        while (true) {
            if (dead.get()) {
                break;
            }
        }
        unsafeTasks.dispose();
        sensorTasks.dispose();
    }

    public static void main(final String[] args) throws InterruptedException, IOException {
        final String app = "rov";
        final HelpFormatter formatter = new HelpFormatter();
        final Option broadcast = Option.builder("b")
            .longOpt("broadcast")
            .hasArg()
            .argName("ADDRESS")
            .desc("use ADDRESS to broadcast messages")
            .required()
            .build();
        final Option serialPort = Option.builder("s")
            .longOpt("serial-port")
            .hasArg()
            .argName("FILE")
            .desc("read and write to FILE as serial device")
            .required()
            .build();
        final Option baudRate = Option.builder("r")
            .type(Integer.class)
            .longOpt("baud-rate")
            .hasArg()
            .argName("BPS")
            .desc("the baud rate to use")
            .required()
            .build();

        final Options options = new Options();
        options.addOption(broadcast);
        options.addOption(serialPort);
        options.addOption(baudRate);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final InetAddress broadcastAddress = InetAddress.getByName(arguments.getOptionValue("b"));
            final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
            final DatagramSocket socket = new DatagramSocket(broadcastPort);
            final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
                socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
            final Serial serial = SerialFactory.createInstance();
            serial.open(arguments.getOptionValue("s"), Integer.parseInt(arguments.getOptionValue("r")));
            final I2CBus bus = I2CFactory.getInstance(ALT_IMU_I2C_BUS);

            final Rov rov = new Rov(Schedulers.io(), Schedulers.computation(),
                eventPublisher,
                new Maestro<>(serial, MAESTRO_DEVICE_NUMBER),
                new AltIMU10v3(bus, ALT_IMU_SA0_HIGH));

            Runtime.getRuntime().addShutdownHook(new Thread(rov::shutdown));

            Logger.info("Started");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
