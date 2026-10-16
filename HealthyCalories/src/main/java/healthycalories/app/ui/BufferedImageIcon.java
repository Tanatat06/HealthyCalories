package main.java.healthycalories.app.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** ไอคอน Swing ที่วาดภาพบัฟเฟอร์โดยปรับขนาดและใช้การเรนเดอร์คุณภาพสูง */
public final class BufferedImageIcon implements Icon {
    private final BufferedImage image;
    private final int width;
    private final int height;

    /** สร้างไอคอนตามขนาดภาพต้นฉบับ */
    public BufferedImageIcon(BufferedImage image) {
        this(requireImage(image), image.getWidth(), image.getHeight());
    }

    /** สร้างไอคอนด้วยขนาดแสดงผลที่กำหนด โดยบังคับให้แต่ละมิติไม่น้อยกว่าหนึ่งพิกเซล */
    public BufferedImageIcon(BufferedImage image, int width, int height) {
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }
        this.image = image;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    /** ตรวจภาพก่อนอ่านขนาดในตัวสร้างที่รับภาพอย่างเดียว */
    private static BufferedImage requireImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }
        return image;
    }

    /** คืนภาพต้นฉบับที่ไอคอนใช้วาด */
    public BufferedImage getImage() {
        return image;
    }

    /** คืนความกว้างแสดงผลของไอคอน */
    @Override
    public int getIconWidth() {
        return width;
    }

    /** คืนความสูงแสดงผลของไอคอน */
    @Override
    public int getIconHeight() {
        return height;
    }

    /** วาดภาพลงในกราฟิกสำเนาเพื่อไม่รบกวนสถานะกราฟิกของผู้เรียก */
    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, x, y, width, height, component);
        g.dispose();
    }
}
