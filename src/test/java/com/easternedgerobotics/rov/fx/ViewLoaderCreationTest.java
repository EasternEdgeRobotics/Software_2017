package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.test.JfxApplicationThreadRule;

import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.awt.GraphicsEnvironment;
import java.util.Collections;

public class ViewLoaderCreationTest {
    @BeforeClass
    public static void assumeHeadless() {
        Assume.assumeFalse("Test only valid on non-headless system", GraphicsEnvironment.isHeadless());
    }

    @Rule
    public final TestRule jfxRule = new JfxApplicationThreadRule();

    @Test
    public final void loadViewCreatesStage() {
        final ViewLoader viewLoader = new ViewLoader(SliderView.class, "test", Collections.emptyMap());
        final Stage stage = new Stage();
        viewLoader.loadMain(stage);

        Assert.assertNotNull("Stage is null", stage);
        Assert.assertNotNull("Scene is null", stage.getScene());
    }

    @Test
    public final void loadViewDoesSetStageEventHandler() {
        final ViewLoader viewLoader = new ViewLoader(SliderView.class, "test", Collections.emptyMap());
        final Stage stage = new Stage();
        viewLoader.loadMain(stage);

        Assert.assertNotNull("EventHandler for window hide is not set", stage.getOnHidden());
        Assert.assertNotNull("EventHandler for window show is not set", stage.getOnShown());
    }

    @Test
    public final void loadViewDoesSetSceneRootNode() {
        final ViewLoader viewLoader = new ViewLoader(SliderView.class, "test", Collections.emptyMap());
        final Stage stage = new Stage();
        viewLoader.loadMain(stage);
        final Parent view = stage.getScene().getRoot();

        Assert.assertNotNull("Scene root node is null", view);
    }
}
