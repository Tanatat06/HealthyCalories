package main.java.healthycalories.app.ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/** กล่องยืนยันแบบกำหนดหน้าตาเอง และคืนผลว่าผู้ใช้กดยืนยันหรือไม่ */
public class ConfirmDialog {
    private static final Color CONFIRM_COLOR = new Color(0x6D, 0xD5, 0x8D);
    private static final Color CANCEL_COLOR = new Color(0xE0, 0x65, 0x68);

    public boolean show(Component parent, String message) {
        Window owner = parent instanceof Window
                ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner);
        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();
        boolean supportsTransparency = device.isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
        dialog.setBackground(supportsTransparency
                ? new Color(0, 0, 0, 0) : new Color(0x50, 0x50, 0x50));

        boolean[] confirmed = {false};
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setColor(new Color(0, 0, 0, 105));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF4, 0xF4, 0xF4));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 38, 38));
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(460, 230));
        card.setBorder(BorderFactory.createEmptyBorder(25, 34, 28, 34));

        JLabel prompt = new JLabel(message, SwingConstants.CENTER);
        prompt.setFont(new Font("Tahoma", Font.PLAIN, 22));
        prompt.setForeground(new Color(0x222222));

        RoundedButton confirm = new RoundedButton("ตกลง", CONFIRM_COLOR);
        RoundedButton cancel = new RoundedButton("ยกเลิก", CANCEL_COLOR);
        confirm.addActionListener(event -> {
            confirmed[0] = true;
            dialog.dispose();
        });
        cancel.addActionListener(event -> dialog.dispose());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 50, 0));
        buttons.setOpaque(false);
        buttons.add(confirm);
        buttons.add(cancel);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(12, 0, 34, 0);
        card.add(prompt, constraints);

        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 0, 0);
        card.add(buttons, constraints);

        overlay.add(card);
        dialog.setContentPane(overlay);
        dialog.getRootPane().setDefaultButton(confirm);
        dialog.getRootPane().registerKeyboardAction(
                event -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (!supportsTransparency) {
            overlay.setOpaque(true);
            overlay.setBackground(new Color(0x50, 0x50, 0x50));
        }
        if (owner != null && owner.isShowing()) {
            Point ownerLocation = owner.getLocationOnScreen();
            dialog.setBounds(ownerLocation.x, ownerLocation.y,
                    owner.getWidth(), owner.getHeight());
        } else {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setBounds(0, 0, screenSize.width, screenSize.height);
        }
        dialog.setVisible(true);
        return confirmed[0];
    }

    private static final class RoundedButton extends JButton {
        private final Color background;

        RoundedButton(String text, Color background) {
            super(text);
            this.background = background;
            setFont(new Font("Tahoma", Font.PLAIN, 20));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160, 64));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? background.darker() : background);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 26, 26));
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
