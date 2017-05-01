package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.control.TwoActionButton;
import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.value.DigitalPinValue;
import com.easternedgerobotics.rov.value.MotionPowerValue;

import rx.Observable;
import rx.Scheduler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ProfileController {
    private final Observable<MotionPowerValue> motion;

    public ProfileController(
        final Arduino arduino,
        final byte[] inputs,
        final byte[] outputs,
        final long profileSwitchDuration,
        final MotionPowerProfile profiles,
        final Observable<MotionPowerValue> motionPowerValues,
        final Scheduler scheduler
    ) {
        final List<TwoActionButton> buttons = Collections
            .unmodifiableList(IntStream.range(0, inputs.length).mapToObj(i -> {
                final Observable<Boolean> click =  arduino
                    .digitalPin(inputs[i])
                    .map(DigitalPinValue::getValue);
                click.map(x -> !x).subscribe(value -> arduino.setPinValue(outputs[i], value));
                return new TwoActionButton(click, profileSwitchDuration, scheduler);
            }).collect(Collectors.toList()));

        IntStream.range(0, inputs.length).forEach(i -> {
            buttons.get(i).shortClick()
                .withLatestFrom(motionPowerValues, (click, value) -> value)
                .forEach(value -> profiles.set(i, value));
        });

        motion = Observable.range(0, inputs.length)
            .flatMap(i -> buttons.get(i).longClick().map(click -> profiles.get(i)));
    }

    public Observable<MotionPowerValue> getMotion() {
        return motion;
    }
}
