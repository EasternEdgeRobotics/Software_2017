package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;

/**
 * A view is a tree of JavaFX {@link javafx.scene.Node}s that can be rendered.
 */
interface View {
    /**
     * Returns the parent node for the view.
     * @return the parent node for the view
     */
    Parent getParent();
}
