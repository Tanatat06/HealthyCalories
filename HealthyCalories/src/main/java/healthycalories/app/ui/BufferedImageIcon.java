package healthycalories.app.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public final class BufferedImageIcon implements Icon {
    private final BufferedImage image;
    private final int width;
    private final int height;

    public BufferedImageIcon(BufferedImage image) {
        this(requireImage(image), image.getWidth(), image.getHeight());
    }

    public BufferedImageIcon(BufferedImage image, int width, int height) {
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }
        this.image = image;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    private static BufferedImage requireImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }
        return image;
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, x, y, width, height, component);
        g.dispose();
    }
}
