package main.java.healthycalories.persistence;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;

public final class PhotoManager {
    private PhotoManager() {
    }

    public static BufferedImage loadImage(String fileName) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()
                || !new File(fileName).getName().equals(fileName)) {
            throw new IllegalArgumentException("Photo filename must be a simple filename");
        }
        File imageFile = findPhotoFile(new File(System.getProperty("user.dir")), fileName);
        if (imageFile == null) {
            File classLocation;
            try {
                classLocation = new File(PhotoManager.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Could not locate the application files", e);
            }
            if (classLocation.isFile()) {
                classLocation = classLocation.getParentFile();
            }
            imageFile = findPhotoFile(classLocation, fileName);
        }
        if (imageFile == null) {
            throw new IOException("Photo file was not found in ProjectFolders"
                    + File.separator + "Photo: " + fileName);
        }
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Photo format is not supported: " + imageFile.getPath());
        }
        return image;
    }

    private static File findPhotoFile(File directory, String fileName) {
        File current = directory;
        while (current != null) {
            File imageFile = new File(current, "ProjectFolders" + File.separator
                    + "Photo" + File.separator + fileName);
            if (imageFile.isFile()) {
                return imageFile;
            }
            current = current.getParentFile();
        }
        return null;
    }

    public static BufferedImage scaleToFit(BufferedImage image, int maxWidth, int maxHeight) {
        if (image == null) {
            throw new IllegalArgumentException("Photo image must not be null");
        }
        if (maxWidth <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("Photo dimensions must be positive");
        }
        double scale = Math.min((double) maxWidth / image.getWidth(),
                (double) maxHeight / image.getHeight());
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return scaled;
    }
}
