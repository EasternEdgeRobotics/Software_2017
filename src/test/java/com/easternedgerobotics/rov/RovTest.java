package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.PWM;
import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;

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

@SuppressWarnings({"checkstyle:magicnumber"})
public class RovTest {
    @Test
    public final void doesInitializeThrustersWithZero() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final MockMaestro maestro = new MockMaestro();
        final Rov rov = new Rov(eventPublisher, maestro);

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
        final Rov rov = new Rov(eventPublisher, maestro);

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
        final Rov rov = new Rov(eventPublisher, maestro);

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
        final Rov rov = new Rov(eventPublisher, maestro);

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
        final Rov rov = new Rov(eventPublisher, maestro);

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
}
