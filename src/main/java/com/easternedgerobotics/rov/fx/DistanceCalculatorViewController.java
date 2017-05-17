package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.config.Configurable;
import com.easternedgerobotics.rov.config.DistanceCalculationConfig;

import javax.inject.Inject;

public final class DistanceCalculatorViewController implements ViewController {
    private final DistanceCalculatorView view;

    private final GalleryView gallery;

    private final DistanceCalculationConfig config;

    @Inject
    public DistanceCalculatorViewController(
        final DistanceCalculatorView view,
        final GalleryView gallery,
        @Configurable("distanceCalculation") final DistanceCalculationConfig config
    ) {
        this.view = view;
        this.gallery = gallery;
        this.config = config;
    }

    @Override
    public void onCreate() {
        gallery.folderLabel.setText(config.imageDirectory());
        view.galleryBorderPane.setCenter(gallery.getParent());
    }
}
