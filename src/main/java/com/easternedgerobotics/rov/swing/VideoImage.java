package com.easternedgerobotics.rov.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JComponent;

public class VideoImage extends JComponent {
    private Image currentImage;

    public VideoImage() {
        setSize(new Dimension(0, 0));
    }

    public final void setImage(final Image image) {
        final Dimension imageSize = new Dimension(image.getWidth(null), image.getHeight(null));
        if (!getSize().equals(imageSize)) {
            setSize(imageSize);
        }

        currentImage = image;
        repaint();
    }

    public final synchronized void paint(final Graphics g) {
        if (currentImage == null) {
            return;
        }

        g.drawImage(currentImage, 0, 0, this);
    }
}
