package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.Config;
import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.config.RovConfig;
import com.easternedgerobotics.rov.control.SixThrusterConfig;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.Accelerometer;
import com.easternedgerobotics.rov.io.Barometer;
import com.easternedgerobotics.rov.io.Bluetooth;
import com.easternedgerobotics.rov.io.BluetoothReader;
import com.easternedgerobotics.rov.io.CpuInformation;
import com.easternedgerobotics.rov.io.Gyroscope;
import com.easternedgerobotics.rov.io.Light;
import com.easternedgerobotics.rov.io.Magnetometer;
import com.easternedgerobotics.rov.io.Motor;
import com.easternedgerobotics.rov.io.PWM;
import com.easternedgerobotics.rov.io.Thermometer;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.io.pololu.AltIMU10v3;
import com.easternedgerobotics.rov.io.pololu.Maestro;
import com.easternedgerobotics.rov.io.pololu.PololuBus;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.LightASpeedValue;
import com.easternedgerobotics.rov.value.LightBSpeedValue;
import com.easternedgerobotics.rov.value.PortAftSpeedValue;
import com.easternedgerobotics.rov.value.PortForeSpeedValue;
import com.easternedgerobotics.rov.value.RasprimeCpuValue;
import com.easternedgerobotics.rov.value.RasprimeHeartbeatValue;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.StarboardAftSpeedValue;
import com.easternedgerobotics.rov.value.StarboardForeSpeedValue;
import com.easternedgerobotics.rov.value.ToolingASpeedValue;
import com.easternedgerobotics.rov.value.ToolingBSpeedValue;
import com.easternedgerobotics.rov.value.ToolingCSpeedValue;
import com.easternedgerobotics.rov.value.TopsideHeartbeatValue;
import com.easternedgerobotics.rov.value.VertAftSpeedValue;
import com.easternedgerobotics.rov.value.VertForeSpeedValue;

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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class Rov {
    private final RovConfig config;

    private final SixThrusterConfig thrusterConfig;

    private final List<Thruster> thrusters;

    private final List<Motor> motors;

    private final List<Light> lights;

    private final Accelerometer accelerometer;

    private final Barometer barometer;

    private final Thermometer thermometer;

    private final Gyroscope gyroscope;

    private final Magnetometer magnetometer;

    private final Bluetooth bluetooth;

    private final EventPublisher eventPublisher;

    private final AtomicBoolean dead = new AtomicBoolean(true);

    private final Subject<Void, Void> killSwitch = PublishSubject.create();

    <AltIMU extends Accelerometer & Barometer & Thermometer & Gyroscope & Magnetometer,
            MaestroChannel extends ADC & PWM> Rov(
        final EventPublisher eventPublisher,
        final List<MaestroChannel> channels,
        final AltIMU imu,
        final Bluetooth bluetooth,
        final RovConfig rovConfig
    ) {
        this.eventPublisher = eventPublisher;
        this.config = rovConfig;

        final PortAftSpeedValue portAft = new PortAftSpeedValue();
        final StarboardAftSpeedValue starboardAft = new StarboardAftSpeedValue();
        final PortForeSpeedValue portFore = new PortForeSpeedValue();
        final StarboardForeSpeedValue starboardFore = new StarboardForeSpeedValue();
        final VertAftSpeedValue vertAft = new VertAftSpeedValue();
        final VertForeSpeedValue vertFore = new VertForeSpeedValue();

        this.thrusterConfig = new SixThrusterConfig(eventPublisher);

        this.motors = Collections.unmodifiableList(Arrays.asList(
            new Motor(
                eventPublisher
                    .valuesOfType(CameraSpeedValueA.class)
                    .startWith(new CameraSpeedValueA())
                    .cast(SpeedValue.class),
                channels.get(config.cameraAMotorChannel())
                    .setOutputRange(new Range(Motor.MAX_REV, Motor.MAX_FWD))),
            new Motor(
                eventPublisher
                    .valuesOfType(CameraSpeedValueB.class)
                    .startWith(new CameraSpeedValueB())
                    .cast(SpeedValue.class),
                channels.get(config.cameraBMotorChannel())
                    .setOutputRange(new Range(Motor.MAX_REV, Motor.MAX_FWD))),
            new Motor(
                eventPublisher
                    .valuesOfType(ToolingASpeedValue.class)
                    .startWith(new ToolingASpeedValue())
                    .cast(SpeedValue.class),
                channels.get(config.toolingAMotorChannel())
                    .setOutputRange(new Range(Motor.MAX_REV, Motor.MAX_FWD))),
            new Motor(
                eventPublisher
                    .valuesOfType(ToolingBSpeedValue.class)
                    .startWith(new ToolingBSpeedValue())
                    .cast(SpeedValue.class),
                channels.get(config.toolingBMotorChannel())
                    .setOutputRange(new Range(Motor.MAX_REV, Motor.MAX_FWD))),
            new Motor(
                eventPublisher
                    .valuesOfType(ToolingCSpeedValue.class)
                    .startWith(new ToolingCSpeedValue())
                    .cast(SpeedValue.class),
                channels.get(config.toolingCMotorChannel())
                    .setOutputRange(new Range(Motor.MAX_REV, Motor.MAX_FWD)))
        ));

        this.thrusters = Collections.unmodifiableList(Arrays.asList(
            new Thruster(
                eventPublisher
                    .valuesOfType(PortAftSpeedValue.class)
                    .startWith(portAft)
                    .cast(SpeedValue.class),
                channels.get(config.portAftChannel())
                    .setOutputRange(new Range(Thruster.MAX_REV, Thruster.MAX_FWD))),
            new Thruster(
                eventPublisher
                    .valuesOfType(StarboardAftSpeedValue.class)
                    .startWith(starboardAft)
                    .cast(SpeedValue.class),
                channels.get(config.starboardAftChannel())
                    .setOutputRange(new Range(Thruster.MAX_FWD, Thruster.MAX_REV))),
            new Thruster(
                eventPublisher
                    .valuesOfType(PortForeSpeedValue.class)
                    .startWith(portFore)
                    .cast(SpeedValue.class),
                channels.get(config.portForeChannel())
                    .setOutputRange(new Range(Thruster.MAX_REV, Thruster.MAX_FWD))),
            new Thruster(
                eventPublisher
                    .valuesOfType(StarboardForeSpeedValue.class)
                    .startWith(starboardFore)
                    .cast(SpeedValue.class),
                channels.get(config.starboardForeChannel())
                    .setOutputRange(new Range(Thruster.MAX_FWD, Thruster.MAX_REV))),
            new Thruster(
                eventPublisher
                    .valuesOfType(VertAftSpeedValue.class)
                    .startWith(vertAft)
                    .cast(SpeedValue.class),
                channels.get(config.vertAftChannel())
                    .setOutputRange(new Range(Thruster.MAX_FWD, Thruster.MAX_REV))),
            new Thruster(
                eventPublisher
                    .valuesOfType(VertForeSpeedValue.class)
                    .startWith(vertFore)
                    .cast(SpeedValue.class),
                channels.get(config.vertForeChannel())
                    .setOutputRange(new Range(Thruster.MAX_REV, Thruster.MAX_FWD)))
        ));

        this.lights = Collections.unmodifiableList(Arrays.asList(
            new Light(
                eventPublisher
                    .valuesOfType(LightASpeedValue.class)
                    .startWith(new LightASpeedValue())
                    .cast(SpeedValue.class),
                channels.get(config.lightAChannel()).setOutputRange(new Range(Light.MAX_REV, Light.MAX_FWD))),
            new Light(
                eventPublisher
                    .valuesOfType(LightBSpeedValue.class)
                    .startWith(new LightBSpeedValue())
                    .cast(SpeedValue.class),
                channels.get(config.lightBChannel()).setOutputRange(new Range(Light.MAX_REV, Light.MAX_FWD)))
        ));

        barometer = () -> imu.pressure();
        magnetometer = () -> imu.rotation();
        accelerometer = () -> imu.acceleration();
        gyroscope = () -> imu.angularVelocity();
        thermometer = () -> imu.temperature();

        this.bluetooth = bluetooth;
    }

    void shutdown() {
        Logger.info("Shutting down");
        final long now = System.currentTimeMillis();
        final long timeout = config.shutdownTimeout();
        killSwitch.onCompleted();
        while (System.currentTimeMillis() - now < timeout) {
            if (dead.get()) {
                break;
            }
        }

        motors.forEach(Motor::writeZero);
        lights.forEach(Light::writeZero);
        thrusters.forEach(Thruster::writeZero);
        bluetooth.stop();
        eventPublisher.emit(new RasprimeHeartbeatValue(false));
    }

    /**
     * Initialises the ROV, attaching the hardware updates to their event source. The ROV will "timeout"
     * if communication with the topside is lost or the received heartbeat value indicates a non-operational
     * status and will shutdown.
     * @param io the scheduler to use for device I/O
     * @param clock the scheduler to use for timing
     */
    void init(final Scheduler io, final Scheduler clock) {
        Logger.debug("Wiring up heartbeat, timeout, and thruster updates");
        final CpuInformation cpuInformation = new CpuInformation(
            RasprimeCpuValue::new, config.cpuPollInterval(), TimeUnit.SECONDS);
        cpuInformation.observe().subscribe(eventPublisher::emit, Logger::error);

        final Observable<TopsideHeartbeatValue> topsideHeartbeats = eventPublisher
            .valuesOfType(TopsideHeartbeatValue.class);

        final Observable<TopsideHeartbeatValue> timeout = Observable.just(new TopsideHeartbeatValue(false))
            .delay(config.maxHeartbeatGap(), TimeUnit.SECONDS, clock)
            .doOnNext(heartbeat -> Logger.warn("Timeout while waiting for heartbeat"))
            .concatWith(Observable.never());

        final Observable<HeartbeatValue> controlHeartbeats = Observable
            .interval(config.sleepDuration(), TimeUnit.MILLISECONDS, clock)
            .withLatestFrom(
                topsideHeartbeats.mergeWith(timeout.takeUntil(topsideHeartbeats).repeat()),
                (tick, heartbeat) -> heartbeat)
            .cast(HeartbeatValue.class)
            .observeOn(io);

        controlHeartbeats.takeUntil(killSwitch)
            .doOnSubscribe(this::doOnSubscribe)
            .subscribe(this::onNext, this::onError, this::onCompleted);

        thrusters.forEach(Thruster::writeZero);

        // Temporarily commented section to prevent logs overflowing. FIX ME!!!

        final Observable<Long> sensorInterval = Observable.interval(
                config.sensorPollInterval(),
                TimeUnit.MILLISECONDS,
                io);
        sensorInterval.subscribe(tick -> {
            eventPublisher.emit(barometer.pressure());
            eventPublisher.emit(accelerometer.acceleration());
            eventPublisher.emit(gyroscope.angularVelocity());
            eventPublisher.emit(magnetometer.rotation());
            eventPublisher.emit(thermometer.temperature());
        });

        bluetooth.start(eventPublisher);
    }

    private void thrustersUpdate() {
        thrusterConfig.update();
        thrusters.forEach(Thruster::write);
    }

    private void softShutdown() {
        thrusterConfig.updateZero();
        thrusters.forEach(Thruster::writeZero);
    }

    private void doOnSubscribe() {
        dead.set(false);
    }

    private void onNext(final HeartbeatValue heartbeat) {
        if (heartbeat.getOperational()) {
            thrustersUpdate();
            lights.forEach(Light::write);
            motors.forEach(Motor::write);
            eventPublisher.emit(new RasprimeHeartbeatValue(true));
        } else {
            softShutdown();
            lights.forEach(Light::flash);
            motors.forEach(Motor::writeZero);
            eventPublisher.emit(new RasprimeHeartbeatValue(false));
        }
    }

    private void onError(final Throwable e) {
        dead.set(true);
        eventPublisher.emit(new RasprimeHeartbeatValue(false));
        throw new RuntimeException(e);
    }

    private void onCompleted() {
        eventPublisher.emit(new RasprimeHeartbeatValue(false));
        dead.set(true);
    }

    public static void main(final String[] args) throws InterruptedException, IOException {
        final String app = "rov";
        final HelpFormatter formatter = new HelpFormatter();
        final Option defaultConfig = Option.builder("d")
            .longOpt("default")
            .hasArg()
            .argName("DEFAULT")
            .desc("name of the default config file")
            .required()
            .build();
        final Option config = Option.builder("c")
            .longOpt("config")
            .hasArg()
            .argName("CONFIG")
            .desc("name of the overriding config file")
            .required()
            .build();

        final Options options = new Options();
        options.addOption(defaultConfig);
        options.addOption(config);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final LaunchConfig launchConfig = new Config(
                arguments.getOptionValue("d"),
                arguments.getOptionValue("c")
            ).getConfig("launch", LaunchConfig.class);

            final InetAddress broadcastAddress = InetAddress.getByName(launchConfig.broadcast());
            final int broadcastPort = launchConfig.defaultBroadcastPort();
            final DatagramSocket socket = new DatagramSocket(broadcastPort);
            final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
                socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
            final Serial serial = SerialFactory.createInstance();
            final RovConfig rovConfig = new Config(
                    arguments.getOptionValue("d"),
                    arguments.getOptionValue("c")
            ).getConfig("rov", RovConfig.class);
            final Rov rov = new Rov(
                eventPublisher,
                new Maestro<>(serial, rovConfig.maestroDeviceNumber()),
                new AltIMU10v3(new PololuBus(rovConfig.i2cBus()), rovConfig.altImuSa0High()),
                new BluetoothReader(
                    rovConfig.bluetoothComPortName(),
                    rovConfig.bluetoothComPort(),
                    rovConfig.bluetoothConnectionTimeout(),
                    rovConfig.bluetoothBaudRate()
                ),
                rovConfig);

            Runtime.getRuntime().addShutdownHook(new Thread(rov::shutdown));

            serial.open(launchConfig.serialPort(), launchConfig.baudRate());
            rov.init(Schedulers.io(), Schedulers.computation());

            Logger.info("Started");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
