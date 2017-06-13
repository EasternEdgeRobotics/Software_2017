package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import javax.inject.Inject;

public class BluetoothView implements View {
    private static final int INITIAL_ROWS = 5;

    private final ScrollPane scrollPane = new ScrollPane();

    private final TextArea textArea;

    @Inject
    public BluetoothView() {
        textArea = new TextArea();
        textArea.setEditable(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(textArea);
    }

    /**
     * Returns the parent node for the view.
     * @return the parent node for the view
     */
    @Override
    public final Parent getParent() {
        return scrollPane;
    }

    /**
     * Adds a new value to the text area.
     * @param value a new string to add
     */
    public void addValue(final String value) {
        textArea.appendText(value + "\n");
    }
}
