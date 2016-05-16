package com.easternedgerobotics.rov.io.firmata;

import com.bortbort.arduino.FiloFirmata.DigitalPinValue;
import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.DigitalPortMessage;

import rx.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DigitalPinListener {

    private DigitalPinListener() {

    }

    /**
     * Observe a Firmata device for changes in digital pin values.
     *
     * @param firmata device to observe.
     * @return observable
     */
    public static Observable<DigitalPin> getPins(final Firmata firmata) {
        final Map<Integer, Integer> values = new HashMap<>();

        return Observable.create(subscriber -> firmata.addMessageListener(new MessageListener<DigitalPortMessage>() {
            @Override
            public void messageReceived(final DigitalPortMessage message) {
                for (final Map.Entry<Integer, DigitalPinValue> entry : message.getDigitalPinValues().entrySet()) {
                    final int port = entry.getKey();
                    final int value = entry.getValue().getIntValue();

                    if (!values.containsKey(port) || !Objects.equals(values.get(port), value)) {
                        values.put(port, value);
                        subscriber.onNext(new DigitalPin(port, value == 1));
                    }
                }
            }
        }));
    }
}
