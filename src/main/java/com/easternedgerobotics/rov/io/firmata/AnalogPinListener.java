package com.easternedgerobotics.rov.io.firmata;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.AnalogMessage;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

public final class AnalogPinListener {

    /**
     * Lower dead-band value.
     */
    private static final float LOWER_CLAMP = 5f;

    /**
     * Upper dead-band value.
     */
    private static final float UPPER_CLAMP = 250f;

    /**
     * Minimum value change required for a change to be registered.
     */
    private static final int STEP_RESOLUTION = 2;

    private AnalogPinListener() {

    }

    /**
     * Observe a Firmata device for changes in analog pin values.
     *
     * @param firmata device to observe.
     * @return observable
     */
    public static Observable<AnalogPin> getPins(final Firmata firmata) {
        final Map<Integer, Integer> values = new HashMap<>();

        return Observable.create(subscriber -> firmata.addMessageListener(new MessageListener<AnalogMessage>() {
            @Override
            public void messageReceived(final AnalogMessage message) {
                final int port = message.getChannelByte();
                final int value = message.getAnalogValue();

                if (!values.containsKey(port) && Math.abs(values.get(port) - value) >= STEP_RESOLUTION) {
                    values.put(port, value);

                    if (value < LOWER_CLAMP) {
                        subscriber.onNext(new AnalogPin(port, 0));
                    } else if (value > UPPER_CLAMP) {
                        subscriber.onNext(new AnalogPin(port, 1));
                    } else {
                        subscriber.onNext(new AnalogPin(port, (value - LOWER_CLAMP) / (UPPER_CLAMP - LOWER_CLAMP)));
                    }
                }
            }
        }));
    }
}
