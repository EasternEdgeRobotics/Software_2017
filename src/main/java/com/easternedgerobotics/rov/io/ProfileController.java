package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.control.TwoActionButton;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.value.DigitalPinValue;
import com.easternedgerobotics.rov.value.GlobalPowerValue;
import com.easternedgerobotics.rov.value.HeavePowerValue;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.PitchPowerValue;
import com.easternedgerobotics.rov.value.RollPowerValue;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.SurgePowerValue;
import com.easternedgerobotics.rov.value.SwayPowerValue;
import com.easternedgerobotics.rov.value.YawPowerValue;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ProfileController {
    private final CompositeSubscription subscription = new CompositeSubscription();

    public ProfileController(
        final Arduino arduino,
        final byte[] inputs,
        final byte[] outputs,
        final int profileSwitchDuration,
        final int profileSaveFlashCount,
        final int profileSaveFlashDuration,
        final EventPublisher eventPublisher,
        final MotionPowerProfile profiles,
        final Scheduler scheduler
    ) {
        final List<TwoActionButton> buttons = Collections
            .unmodifiableList(IntStream.range(0, outputs.length).mapToObj(i -> {
                final Observable<Boolean> click =  arduino
                    .digitalPin(inputs[i])
                    .map(DigitalPinValue::getValue);
                subscription.add(click.map(x -> !x).subscribe(value -> arduino.setPinValue(outputs[i], value)));
                return new TwoActionButton(click, profileSwitchDuration, scheduler);
            }).collect(Collectors.toList()));

        IntStream.range(0, outputs.length).forEach(i -> subscription.add(buttons.get(i).longClick().filter(x -> x)
            .withLatestFrom(
                Observable.combineLatest(
                    eventPublisher.valuesOfType(GlobalPowerValue.class).map(SpeedValue::getSpeed),
                    eventPublisher.valuesOfType(HeavePowerValue.class).map(SpeedValue::getSpeed),
                    eventPublisher.valuesOfType(SwayPowerValue.class).map(SpeedValue::getSpeed),
                    eventPublisher.valuesOfType(SurgePowerValue.class).map(SpeedValue::getSpeed),
                    eventPublisher.valuesOfType(PitchPowerValue.class).map(SpeedValue::getSpeed),
                    eventPublisher.valuesOfType(YawPowerValue.class).map(SpeedValue::getSpeed),
                    eventPublisher.valuesOfType(RollPowerValue.class).map(SpeedValue::getSpeed),
                    MotionPowerValue::new
                ), (click, value) -> value)
            .subscribe(value -> {
                profiles.set(i, value);
                arduino.setPinValue(outputs[i], true);
                Observable.interval(profileSaveFlashDuration, TimeUnit.MILLISECONDS, Schedulers.io())
                    .take(profileSaveFlashCount)
                    .subscribe(tick -> arduino.setPinValue(outputs[i], tick % 2 == 0));
            })));

        subscription.add(Observable.range(0, outputs.length)
            .flatMap(i -> buttons.get(i).shortClick().filter(x -> x).map(click -> profiles.get(i)))
            .subscribe(value -> {
                eventPublisher.emit(new GlobalPowerValue(value.getGlobal()));
                eventPublisher.emit(new HeavePowerValue(value.getHeave()));
                eventPublisher.emit(new SwayPowerValue(value.getSway()));
                eventPublisher.emit(new SurgePowerValue(value.getSurge()));
                eventPublisher.emit(new PitchPowerValue(value.getPitch()));
                eventPublisher.emit(new YawPowerValue(value.getYaw()));
                eventPublisher.emit(new RollPowerValue(value.getRoll()));
            }));
    }

    public void stop() {
        subscription.clear();
    }
}
