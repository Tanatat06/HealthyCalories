package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.font.TextLayout;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class RegisterScreen extends BaseScreen {

    public RegisterScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void onRegister() {
        frame.showScreen(MainFrame.ONBOARDING);
    }

    public void onBack() {
        frame.showScreen(MainFrame.LOGIN);
    }

    @Override
    public void buildUI() {
        removeAll();
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(420, 560));

        JLabel title = new JLabel("สร้างบัญชี");
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        HintTextField emailField = new HintTextField("อีเมล");
        emailField.setName("emailField");
        HintPasswordField passwordField = new HintPasswordField("รหัสผ่าน");
        passwordField.setName("passwordField");
        HintPasswordField confirmPasswordField = new HintPasswordField("ยืนยันรหัสผ่าน");
        confirmPasswordField.setName("confirmPasswordField");

        RoundedButton registerButton = new RoundedButton(
                "สร้างบัญชี", new Color(240, 100, 104));
        registerButton.addActionListener(e -> onRegister());

        RoundedButton backButton = new RoundedButton(
                "ย้อนกลับ", new Color(112, 215, 140));
        backButton.addActionListener(e -> onBack());

        form.add(title);
        form.add(Box.createVerticalStrut(112));
        form.add(emailField);
        form.add(Box.createVerticalStrut(40));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(40));
        form.add(confirmPasswordField);
        form.add(Box.createVerticalStrut(48));
        form.add(registerButton);
        form.add(Box.createVerticalStrut(18));
        form.add(backButton);

        add(form);
        revalidate();
        repaint();
    }

    private static class HintTextField extends JTextField {
        private final String hint;
        private boolean focused;

        HintTextField(String hint) {
            this.hint = hint;
            setFont(new Font("Tahoma", Font.PLAIN, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new MatteBorder(0, 0, 1, 0, new Color(170, 170, 170)));
            setPreferredSize(new Dimension(240, 36));
            setMinimumSize(new Dimension(120, 36));
            getAccessibleContext().setAccessibleName(hint);
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent event) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent event) {
                    focused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !focused) {
                paintHint(graphics, this, hint);
            }
        }
    }

    private static class HintPasswordField extends JPasswordField {
        private final String hint;
        private boolean focused;

        HintPasswordField(String hint) {
            this.hint = hint;
            setFont(new Font("Tahoma", Font.PLAIN, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new MatteBorder(0, 0, 1, 0, new Color(170, 170, 170)));
            setPreferredSize(new Dimension(240, 36));
            setMinimumSize(new Dimension(120, 36));
            getAccessibleContext().setAccessibleName(hint);
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent event) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent event) {
                    focused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getPassword().length == 0 && !focused) {
                paintHint(graphics, this, hint);
            }
        }
    }

    private static void paintHint(Graphics graphics, JComponent component, String hint) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        TextLayout layout = new TextLayout(hint, component.getFont(), g2.getFontRenderContext());
        float x = (component.getWidth() - layout.getAdvance()) / 2;
        float y = (component.getHeight() - layout.getAscent() - layout.getDescent()) / 2
                + layout.getAscent();
        g2.setColor(new Color(80, 80, 80));
        layout.draw(g2, x, y);
        g2.dispose();
    }

    private static class RoundedButton extends JButton {
        private final Color backgroundColor;

        RoundedButton(String text, Color backgroundColor) {
            super(text);
            this.backgroundColor = backgroundColor;
            setFont(new Font("Tahoma", Font.PLAIN, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setPreferredSize(new Dimension(220, 42));
            setMaximumSize(new Dimension(220, 42));
            setMinimumSize(new Dimension(220, 42));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
