package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.AutoDepthValue;

import rx.observables.JavaFxObservable;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public class AutoDepthViewController implements ViewController{
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    private final AutoDepthView view;

    private final EventPublisher eventPublisher;

    @Inject
    public AutoDepthViewController(
        final AutoDepthView view,
        final EventPublisher eventPublisher
    ) {
        this.view = view;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onCreate() {
        subscriptions.add(JavaFxObservable.valuesOf(view.button.selectedProperty())
            .subscribe(this::onSelected));
    }

    private void onSelected(final boolean selected) {
        if (!selected) {
            eventPublisher.emit(new AutoDepthValue(0, false));
            return;
        }
        final String depthString = view.text.getText();
        try {
            final float depth = Float.parseFloat(depthString);
            eventPublisher.emit(new AutoDepthValue(depth, true));
        } catch(final NumberFormatException e) {
            // ignore
        }
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
    }
}
