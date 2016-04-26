package com.easternedgerobotics.rov.swing;

import java.awt.GridLayout;
import java.awt.Image;
import java.util.function.Consumer;
import javax.swing.JFrame;

public class VideoFrame extends JFrame {
    /**
     * The contained video feeds.
     */
    private final VideoImage[] feeds;

    /**
     * Creates a VideoFrame with the given number of rows and columns.
     */
    public VideoFrame(final int rows, final int cols) {
        super("Video");

        feeds = new VideoImage[rows * cols];
        setLayout(new GridLayout(rows, cols));
        for (int i = 0; i < (rows * cols); i++) {
            feeds[i] = new VideoImage();
            add(feeds[i]);
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Returns an update function for the given video feed index.
     *
     * @param index The clockwise index of the video feed
     */
    public final Consumer<Image> updateImageByIndex(final int index) {
        return feeds[index]::setImage;
    }
}
