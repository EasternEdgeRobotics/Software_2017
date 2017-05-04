package com.easternedgerobotics.rov.fx;

import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public final class ViewLauncher {
    private final Map<Class<? extends View>, Stage> stages = new HashMap<>();

    private ViewLoader viewLoader;

    private Stage stage;

    public void start(
        final ViewLoader viewLoader,
        final Stage stage,
        final Class<? extends View> mainView,
        final String title
    ) {
        this.viewLoader = viewLoader;
        this.stage = stage;

        viewLoader.loadIntoStage(mainView, stage);
        stage.setTitle(title);
        stage.show();

        stages.put(mainView, stage);
    }

    public void launch(
        final Class<? extends View> view,
        final String title
    ) {
        if (!stages.containsKey(view) || !stages.get(view).isShowing()) {
            if (stages.containsKey(view)) {
                stages.get(view).close();
            }
            final Stage subStage = viewLoader.load(view);
            subStage.setTitle(title);
            subStage.initOwner(stage);
            subStage.show();

            stages.put(view, subStage);
        } else {
            stages.get(view).show();
        }
    }
}
