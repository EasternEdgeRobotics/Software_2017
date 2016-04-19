package com.easternedgerobotics.rov.fx;

import javafx.beans.value.ObservableValue;

import javax.inject.Inject;

public class SliderViewController implements ViewController {
    private final SliderView view;

    @Inject
    public SliderViewController(final SliderView view) {
        this.view = view;
    }

    @Override
    public final void onCreate() {
        view.valueLabel.setText(String.format(SliderView.LABEL_FORMAT, view.slider.getValue()));
        view.slider.valueProperty().addListener(this::changed);
    }

    private void changed(final ObservableValue<? extends Number> value, final Number old, final Number current) {
        view.valueLabel.setText(String.format(SliderView.LABEL_FORMAT, current.doubleValue()));
    }
}
