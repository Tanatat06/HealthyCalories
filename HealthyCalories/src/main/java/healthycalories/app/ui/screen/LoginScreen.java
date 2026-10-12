package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class LoginScreen extends BaseScreen {

    private HintTextField emailField;
    private HintPasswordField passwordField;

    public LoginScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void onLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            if (email.isEmpty() || password.isEmpty()) {
                frame.showDailog("กรุณากรอกอีเมลและรหัสผ่าน");
                return;
            }
            if (!app.getMemberRepository().authenticate(email, password)) {
                frame.showDailog("ไม่พบบัญชีสมาชิกหรืออีเมล/รหัสผ่านไม่ถูกต้อง");
                return;
            }
            frame.showScreen(MainFrame.HOME);
        } catch (IOException e) {
            frame.showDailog("ไม่สามารถตรวจสอบข้อมูลสมาชิกได้: " + e.getMessage());
        } finally {
            passwordField.setText("");
        }
    }

    public void onGoRegister() {
        frame.showScreen(MainFrame.REGISTER);
    }

    @Override
    public void buildUI() {
        removeAll();
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(420, 610));

        JPanel foodArt = new JPanel(new BorderLayout());
        foodArt.setOpaque(false);
        foodArt.setPreferredSize(new Dimension(420, 150));
        JLabel bottle = loadFoodImage("Register1.png", 52);
        JLabel apple = loadFoodImage("Register2.png", 52);
        JPanel artRow = new JPanel(new BorderLayout());
        artRow.setOpaque(false);
        artRow.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 18));
        artRow.add(bottle, BorderLayout.WEST);
        artRow.add(apple, BorderLayout.EAST);
        foodArt.add(artRow, BorderLayout.CENTER);

        emailField = new HintTextField("อีเมล");
        emailField.setName("emailField");
        passwordField = new HintPasswordField("รหัสผ่าน");
        passwordField.setName("passwordField");

        RoundedButton loginButton = new RoundedButton(
                "เข้าสู่ระบบ", new Color(221, 185, 231));
        loginButton.addActionListener(e -> onLogin());

        JPanel divider = new JPanel(new GridBagLayout());
        divider.setOpaque(false);
        JSeparator leftLine = new JSeparator();
        JSeparator rightLine = new JSeparator();
        JLabel orLabel = new JLabel("หรือ");
        orLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        divider.add(leftLine, constraints);
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 16, 0, 16);
        divider.add(orLabel, constraints);
        constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        divider.add(rightLine, constraints);

        RoundedButton registerButton = new RoundedButton(
                "สร้างบัญชี", new Color(200, 119, 121));
        registerButton.addActionListener(e -> onGoRegister());

        RoundedButton backButton = new RoundedButton(
                "ย้อนกลับ", new Color(112, 215, 140));
        backButton.addActionListener(e -> frame.showScreen(MainFrame.SPLASH));

        form.add(foodArt);
        form.add(Box.createVerticalStrut(4));
        form.add(emailField);
        form.add(Box.createVerticalStrut(28));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(20));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(24));
        form.add(divider);
        form.add(Box.createVerticalStrut(24));
        form.add(registerButton);
        form.add(Box.createVerticalStrut(16));
        form.add(backButton);

        add(form);
        revalidate();
        repaint();
    }

    private JLabel loadFoodImage(String name, int size) {
        String resource = "/healthycalories/app/ui/screen/images/" + name;
        try {
            java.net.URL url = getClass().getResource(resource);
            BufferedImage image = url == null ? null : ImageIO.read(url);
            if (image == null) {
                File file = new File("ProjectFolders" + File.separator + "Photo", name);
                if (file.isFile()) image = ImageIO.read(file);
            }
            if (image == null) throw new IllegalStateException("ไม่พบรูปอาหาร: " + name);
            double scale = Math.min((double) size / image.getWidth(), (double) size / image.getHeight());
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
            return new JLabel(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        } catch (IOException e) {
            throw new IllegalStateException("อ่านรูปอาหารไม่สำเร็จ: " + name, e);
        }
    }

    private static class HintTextField extends JTextField {
        private final String hint;
        private boolean focused;

        HintTextField(String hint) {
            this.hint = hint;
            setFont(new Font("Tahoma", Font.PLAIN, 16));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new MatteBorder(0, 0, 1, 0, new Color(150, 150, 150)));
            setPreferredSize(new Dimension(340, 38));
            setMinimumSize(new Dimension(180, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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
            setFont(new Font("Tahoma", Font.PLAIN, 16));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new MatteBorder(0, 0, 1, 0, new Color(150, 150, 150)));
            setPreferredSize(new Dimension(340, 38));
            setMinimumSize(new Dimension(180, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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
        g2.setColor(new Color(70, 70, 70));
        layout.draw(g2, x, y);
        g2.dispose();
    }

    private static class RoundedButton extends JButton {
        private final Color backgroundColor;

        RoundedButton(String text, Color backgroundColor) {
            super(text);
            this.backgroundColor = backgroundColor;
            setFont(new Font("Tahoma", Font.PLAIN, 16));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setPreferredSize(new Dimension(300, 44));
            setMaximumSize(new Dimension(300, 44));
            setMinimumSize(new Dimension(300, 44));
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
