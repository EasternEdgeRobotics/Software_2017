package com.easternedgerobotics.rov.io;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.pmw.tinylog.Logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.imageio.ImageIO;

public final class FileUtil {
    private FileUtil() {

    }

    /**
     * Given a folder and a base name, append a number to the file name until the filename is unique.
     *
     * @param folderName the desired folder
     * @param name the base file name
     * @param type the file extension
     * @return a unique file name
     */
    public static File nextName(final String folderName, final String name, final String type) {
        final File folder = new File(folderName);
        if (!folder.exists()) {
            return null;
        }
        for (int i = 0;; i++) {
            final File file = new File(folder, String.format("%s_%d.%s", name, i, type));
            if (!file.exists()) {
                return file;
            }
        }
    }

    /**
     * Save an image to a destination output file with the provided extension.
     *
     * @param image the image to save
     * @param outputFile the image destination
     * @param type the extension
     */
    public static void saveImageFile(final Image image, final File outputFile, final String type) {
        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bufferedImage, type, outputFile);
        } catch (final IOException e) {
            Logger.error(e);
        }
    }

    /**
     * Given a set of file names and a directory name, copy the supplied files to this directory.
     *
     * @param fileNames the files to be copied.
     * @param directoryName the destination.
     */
    public static void copyFilesToDirectory(final List<String> fileNames, final String directoryName) {
        final Path directory = Paths.get(directoryName);
        DirectoryUtil.clearDirectory(directory);
        fileNames.stream().map(Paths::get).forEach(path -> {
            final Path copyPath = directory.resolve(path.getName(path.getNameCount() - 1));
            try {
                Files.copy(path, copyPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                Logger.error(e);
            }
        });
    }
}
