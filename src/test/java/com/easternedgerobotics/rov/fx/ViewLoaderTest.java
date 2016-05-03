package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.test.JfxApplicationThreadRule;

import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

public class ViewLoaderTest {
    @Rule
    public JfxApplicationThreadRule jfxRule = new JfxApplicationThreadRule();

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
}
