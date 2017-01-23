package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.test.JfxApplicationThreadRule;

import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.prefs.BackingStoreException;

public class ViewLoaderTest {
    @Rule
    public JfxApplicationThreadRule jfxRule = new JfxApplicationThreadRule();

    private static final double DELTA = 0.00001;

    @Test
    public final void loadViewCreatesStage() {
        final ViewLoader viewLoader = new ViewLoader(Collections.emptyMap());
        final Stage stage = viewLoader.load(SliderView.class);

        Assert.assertNotNull("Stage is null", stage);
        Assert.assertNotNull("Scene is null", stage.getScene());
    }

    @Test
    public final void loadViewDoesSetStageEventHandler() {
        final ViewLoader viewLoader = new ViewLoader(Collections.emptyMap());
        final Stage stage = viewLoader.load(SliderView.class);

        Assert.assertNotNull("EventHandler for window hide is not set", stage.getOnHidden());
        Assert.assertNotNull("EventHandler for window show is not set", stage.getOnShown());
    }

    @Test
    public final void loadViewDoesSetSceneRootNode() {
        final ViewLoader viewLoader = new ViewLoader(Collections.emptyMap());
        final Stage stage = viewLoader.load(SliderView.class);
        final Parent view = stage.getScene().getRoot();

        Assert.assertNotNull("Scene root node is null", view);
    }

    @Test
    public final void doesSaveSizeLocation() {
        final ViewLoader viewLoader1 = new ViewLoader(Collections.emptyMap());
        final Stage stage1 = viewLoader1.load(SliderView.class);
        // Get current values and modify
        stage1.getOnShown().handle(null);
        final double x = stage1.getX();
        final double y = stage1.getY();
        final double w = stage1.getWidth();
        final double h = stage1.getHeight();
        stage1.setX(x * 2);
        stage1.setY(y * 2);
        stage1.setWidth(w * 2);
        stage1.setHeight(h * 2);
        stage1.getOnHidden().handle(null);
        // Get newest values from a separate loader
        final ViewLoader viewLoader2 = new ViewLoader(Collections.emptyMap());
        final Stage stage2 = viewLoader2.load(SliderView.class);
        stage2.getOnShown().handle(null);
        Assert.assertEquals(x * 2, stage2.getX(), DELTA);
        Assert.assertEquals(y * 2, stage2.getY(), DELTA);
        Assert.assertEquals(w * 2, stage2.getWidth(), DELTA);
        Assert.assertEquals(h * 2, stage2.getHeight(), DELTA);
    }

    @Test
    public final void doesResetSizeLocation() throws BackingStoreException {
        final ViewLoader viewLoader = new ViewLoader(Collections.emptyMap());
        final Stage stage = viewLoader.load(SliderView.class);
        // Get current values and modify
        stage.getOnShown().handle(null);
        final double x = stage.getX();
        final double y = stage.getY();
        final double w = stage.getWidth();
        final double h = stage.getHeight();
        stage.setX(x * 2);
        stage.setY(y * 2);
        stage.setWidth(w * 2);
        stage.setHeight(h * 2);
        stage.getOnHidden().handle(null);
        ViewLoader.dropPreferences();
        // force reload of values
        stage.getOnShown().handle(null);
        Assert.assertEquals(x, stage.getX(), DELTA);
        Assert.assertEquals(y, stage.getY(), DELTA);
        Assert.assertEquals(w, stage.getWidth(), DELTA);
        Assert.assertEquals(h, stage.getHeight(), DELTA);
    }
}
