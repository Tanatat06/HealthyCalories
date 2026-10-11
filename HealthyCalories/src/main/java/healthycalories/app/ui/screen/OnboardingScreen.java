package main.java.healthycalories.app.ui.screen;

import javax.swing.*;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class OnboardingScreen extends BaseScreen {

    // ---------- สี / ฟอนต์ ----------
    private static final Color BG = Color.WHITE;
    private static final Color PINK = new Color(0xEE, 0xA8, 0xF2);
    private static final Color PURPLE = new Color(0xDC, 0xC0, 0xEA);
    private static final Color SELECTED = new Color(0xD9, 0xBF, 0xEA);
    private static final Font FONT_TITLE = new Font("Tahoma", Font.BOLD, 20);
    private static final Font FONT_TEXT = new Font("Tahoma", Font.PLAIN, 15);
    private static final Font FONT_SMALL = new Font("Tahoma", Font.PLAIN, 12);

    // ---------- ชื่อหน้า (CardLayout) ----------
    private static final String PAGE_GENDER = "gender";
    private static final String PAGE_BODY = "body";
    private static final String PAGE_GOAL = "goal";
    private static final String PAGE_RESULT = "result";

    // ---------- ตัวเลือกความเร็ว (กก./สัปดาห์) ----------
    private static final String[] GOAL_NAMES = {
            "แบบรวดเร็วมาก", "แบบเร็ว", "แบบธรรมดา (แนะนำ)", "แบบค่อยๆ" };
    private static final double[] GOAL_KG = { 1.0, 0.5, 0.33, 0.25 };

    // ---------- state ----------
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private int step = 0; // 0 = เพศ, 1 = ข้อมูลร่างกาย, 2 = เป้าหมาย, 3 = ผลลัพธ์
    private JLabel resultKcalLabel, resultDaysLabel;
    private int daysToGoal;

    private boolean isMale = false;
    private boolean genderChosen = false;
    private int selectedGoal = 2; // ค่าเริ่มต้น = แบบธรรมดา (แนะนำ)

    private JTextField ageField, heightField, weightField, targetField;
    private JLabel bodyAvatar;
    private JButton femaleBtn, maleBtn;
    private JButton nextBtn, backBtn;
    private JButton[] goalButtons;
    private JPanel dotsPanel, actionRow, actionColumn;

    // ผลลัพธ์ล่าสุด
    private double bmr, tdee, targetCalories, weeklyKg;

    public OnboardingScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        buildUI();
    }

    // =====================================================
    //                       UI
    // =====================================================
    @Override
    public void buildUI() {
        removeAll();
        cards.removeAll();
        setLayout(new BorderLayout());
        setBackground(BG);

        cards.setOpaque(false);
        cards.add(buildGenderPage(), PAGE_GENDER);
        cards.add(buildBodyPage(), PAGE_BODY);
        cards.add(buildGoalPage(), PAGE_GOAL);
        cards.add(buildResultPage(), PAGE_RESULT);
        add(cards, BorderLayout.CENTER);

        // ---- ส่วนล่าง: จุดบอกหน้า + ปุ่ม ----
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 60, 30, 60));

        dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        dotsPanel.setOpaque(false);
        bottom.add(dotsPanel);
        bottom.add(Box.createVerticalStrut(15));

        nextBtn = makeRoundButton("ถัดไป", PURPLE);
        backBtn = makeRoundButton("ย้อนกลับ", PINK);
        backBtn.setPreferredSize(new Dimension(160, 42));
        backBtn.setMinimumSize(new Dimension(160, 42));
        backBtn.setMaximumSize(new Dimension(160, 42));
        nextBtn.addActionListener(e -> next());
        backBtn.addActionListener(e -> back());

        actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
        actionRow.setOpaque(false);
        actionRow.add(backBtn);
        actionRow.add(nextBtn);
        bottom.add(actionRow);

        actionColumn = new JPanel();
        actionColumn.setOpaque(false);
        actionColumn.setLayout(new BoxLayout(actionColumn, BoxLayout.Y_AXIS));
        nextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottom.add(actionColumn);

        add(bottom, BorderLayout.SOUTH);

        updateStepUI();
    }

    /** หน้า 1: เลือกเพศ */
    private JPanel buildGenderPage() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(32, 20, 10, 20));

        Font welcomeFont = new Font("Tahoma", Font.PLAIN, 36);
        JLabel welcomeTitle = new JLabel("ยินดีต้อนรับสู่", SwingConstants.CENTER);
        welcomeTitle.setFont(welcomeFont);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appTitle = new JLabel("HealthyCalories !", SwingConstants.CENTER);
        appTitle.setFont(welcomeFont);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("กรุณาบอกเราเกี่ยวกับตัวคุณสักนิด");
        sub.setFont(new Font("Tahoma", Font.PLAIN, 17));
        sub.setForeground(new Color(0x55, 0x55, 0x55));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel avatars = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        avatars.setOpaque(false);
        avatars.add(makeAvatar("/healthycalories/app/ui/screen/images/Welcome.png", "", 260));

        femaleBtn = makeGenderButton("/healthycalories/app/ui/screen/images/Female.jpg",
                new Color(0xE8, 0x7A, 0x90));
        maleBtn = makeGenderButton("/healthycalories/app/ui/screen/images/Male.jpg",
                new Color(0x6F, 0x8F, 0xD8));
        femaleBtn.addActionListener(e -> {
            isMale = false;
            chooseGender();
        });
        maleBtn.addActionListener(e -> {
            isMale = true;
            chooseGender();
        });

        JPanel genderRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 52, 0));
        genderRow.setOpaque(false);
        genderRow.add(femaleBtn);
        genderRow.add(maleBtn);

        p.add(welcomeTitle);
        p.add(Box.createVerticalStrut(0));
        p.add(appTitle);
        p.add(Box.createVerticalStrut(5));
        p.add(sub);
        p.add(Box.createVerticalStrut(12));
        p.add(avatars);
        p.add(Box.createVerticalStrut(0));
        p.add(genderRow);
        return p;
    }

    /** หน้า 2: กรอกข้อมูลร่างกาย */
    private JPanel buildBodyPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 10, 40));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("เพื่อการคำนวณแคลอรี่ที่เหมาะสมสำหรับคุณ");
        title.setFont(FONT_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("กรุณาบอกเราเกี่ยวกับตัวคุณ");
        sub.setFont(FONT_SMALL);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        bodyAvatar = new JLabel("", SwingConstants.CENTER);
        bodyAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        bodyAvatar.setPreferredSize(new Dimension(270, 270));

        head.add(title);
        head.add(sub);
        head.add(Box.createVerticalStrut(15));
        head.add(bodyAvatar);
        head.add(Box.createVerticalStrut(15));

        ageField = new JTextField(8);
        heightField = new JTextField(8);
        weightField = new JTextField(8);
        targetField = new JTextField(8);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addFormRow(form, 0, "อายุ", ageField, "ปี");
        addFormRow(form, 1, "ส่วนสูง", heightField, "เซนติเมตร");
        addFormRow(form, 2, "น้ำหนัก", weightField, "กิโลกรัม");
        addFormRow(form, 3, "น้ำหนักที่ต้องการ", targetField, "กิโลกรัม");

        p.add(head, BorderLayout.NORTH);
        p.add(form, BorderLayout.CENTER);
        return p;
    }

    /** หน้า 3: เลือกความเร็วในการลดน้ำหนัก */
    private JPanel buildGoalPage() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(30, 50, 10, 50));

        JLabel title = new JLabel("ผลลัพธ์ที่ต้องการเห็นในแต่ละอาทิตย์");
        title.setFont(FONT_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalGlue());

        goalButtons = new JButton[GOAL_NAMES.length];
        for (int i = 0; i < GOAL_NAMES.length; i++) {
            final int idx = i;
            JButton b = new JButton("<html><center>" + GOAL_NAMES[i]
                    + "<br><font size='2' color='gray'>น้ำหนักลดอาทิตย์ละ "
                    + GOAL_KG[i] + " กก.</font></center></html>");
            b.setFont(FONT_TEXT);
            b.setFocusPainted(false);
            b.setBackground(Color.WHITE);
            b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(e -> {
                selectedGoal = idx;
                chooseGoal();
            });
            goalButtons[i] = b;
            p.add(b);
            p.add(Box.createVerticalStrut(8));
        }
        chooseGoal(); // ไฮไลต์ค่าเริ่มต้น
        return p;
    }

    /** หน้า 4: สรุปแคลอรี่ต่อวัน */
    private JPanel buildResultPage() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("มาเริ่มนับแคลอรี่เพื่อไปให้ถึงเป้าหมาย!");
        title.setFont(FONT_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // กล่องสรุปผล
        JPanel box = new JPanel();
        box.setBackground(BG);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));
        box.setMaximumSize(new Dimension(320, 240));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l1 = new JLabel("ปริมาณแคลอรี่ที่ควรบริโภคต่อวันคือ");
        l1.setFont(FONT_SMALL);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultKcalLabel = new JLabel("0");
        resultKcalLabel.setFont(new Font("Tahoma", Font.PLAIN, 56));
        resultKcalLabel.setForeground(new Color(0xF0, 0xC8, 0x80));

        JLabel kcal = new JLabel("kcal");
        kcal.setFont(FONT_SMALL);
        kcal.setForeground(new Color(0xF0, 0xC8, 0x80));

        JPanel numRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        numRow.setOpaque(false);
        numRow.add(resultKcalLabel);
        numRow.add(kcal);

        JLabel l2 = new JLabel("และสามารถได้น้ำหนักตามเป้าหมายภายใน");
        l2.setFont(FONT_SMALL);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultDaysLabel = new JLabel("- วัน!");
        resultDaysLabel.setFont(FONT_SMALL);
        resultDaysLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(l1);
        box.add(Box.createVerticalStrut(10));
        box.add(numRow);
        box.add(Box.createVerticalStrut(10));
        box.add(l2);
        box.add(Box.createVerticalStrut(4));
        box.add(resultDaysLabel);

        content.add(title);
        content.add(Box.createVerticalStrut(40));
        content.add(box);
        p.add(content);
        return p;
    }

    // =====================================================
    //                    Actions
    // =====================================================

    /** เลือกเพศ แล้วไปหน้าถัดไป */
    public void chooseGender() {
        genderChosen = true;
        highlightGender();
        next();
    }

    /** ตรวจและบันทึกข้อมูลร่างกาย (คืน true ถ้าข้อมูลถูกต้อง) */
    public void submitBodyInfo() {
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            double target = Double.parseDouble(targetField.getText().trim());

            if (age < 10 || age > 100 || height < 100 || height > 250
                    || weight < 20 || weight > 300 || target < 20 || target > 300) {
                throw new NumberFormatException();
            }

            // Mifflin-St Jeor
            bmr = 10 * weight + 6.25 * height - 5 * age + (isMale ? 5 : -161);
            tdee = bmr * 1.2; // ไม่ได้ถามระดับกิจกรรม ใช้ค่าคนนั่งทำงานเป็นฐาน
            currentWeight = weight;
            targetWeight = target;
            bodyInfoValid = true;
        } catch (NumberFormatException ex) {
            bodyInfoValid = false;
            JOptionPane.showMessageDialog(this,
                    "กรุณากรอกข้อมูลเป็นตัวเลขให้ครบและถูกต้อง",
                    "ข้อมูลไม่ถูกต้อง", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double currentWeight, targetWeight;
    private boolean bodyInfoValid = false;

    /** เลือกความเร็วในการลดน้ำหนัก */
    public void chooseGoal() {
        for (int i = 0; i < goalButtons.length; i++) {
            goalButtons[i].setBackground(i == selectedGoal ? SELECTED : Color.WHITE);
        }
        weeklyKg = GOAL_KG[selectedGoal];
    }

    /** คำนวณและแสดงผล */
    public void showResult() {
        // 1 กก. ≈ 7,700 แคลอรี่
        double dailyChange = weeklyKg * 7700 / 7.0;
        boolean lose = targetWeight < currentWeight;
        boolean same = Math.abs(targetWeight - currentWeight) < 0.01;

        if (same) {
            targetCalories = tdee;
        } else if (lose) {
            targetCalories = tdee - dailyChange;
        } else {
            targetCalories = tdee + dailyChange;
        }

        // ไม่ให้ต่ำกว่าเกณฑ์ปลอดภัย
        double minSafe = isMale ? 1500 : 1200;
        boolean adjusted = false;
        if (targetCalories < minSafe) {
            targetCalories = minSafe;
            adjusted = true;
        }

        double diff = Math.abs(targetWeight - currentWeight);
        double perDay = Math.abs(tdee - targetCalories); // kcal ที่ขาด/เกินต่อวัน
        daysToGoal = (same || perDay <= 0) ? 0 : (int) Math.ceil(diff * 7700 / perDay);

        resultKcalLabel.setText(String.format("%.0f", targetCalories));
        resultDaysLabel.setText(daysToGoal > 0 ? daysToGoal + " วัน!" : "ถึงเป้าหมายแล้ว!");

        if (adjusted) {
            JOptionPane.showMessageDialog(this,
                    "ปรับเป็นแคลอรี่ขั้นต่ำที่ปลอดภัยแล้ว\n(ลองเลือกแบบที่ค่อยเป็นค่อยไปจะเหมาะกว่า)");
        }

        step = 3;
        updateStepUI();
    }

    /** กด "พร้อมแล้ว!" เพื่อเริ่มใช้งาน */
    public void finish() {
        // TODO: บันทึกข้อมูลผู้ใช้ (isMale, currentWeight, targetWeight, targetCalories)
        frame.showScreen(MainFrame.HOME);
    }

    /** ปุ่มถัดไป */
    public void next() {
        if (step == 0) {
            if (!genderChosen) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกเพศก่อน");
                return;
            }
            step = 1;
        } else if (step == 1) {
            submitBodyInfo();
            if (!bodyInfoValid) return;
            step = 2;
        } else if (step == 2) {
            showResult();
            return;
        } else if (step == 3) {
            finish();
            return;
        }
        updateStepUI();
    }

    /** ปุ่มย้อนกลับ */
    public void back() {
        if (step > 0) {
            step--;
            updateStepUI();
        } else {
            String previous = frame.getPreviousScreen();
            frame.showScreen(previous != null ? previous : MainFrame.SPLASH);
        }
    }

    // =====================================================
    //                    Helpers
    // =====================================================

    private void updateStepUI() {
        String[] pages = { PAGE_GENDER, PAGE_BODY, PAGE_GOAL, PAGE_RESULT };
        cardLayout.show(cards, pages[step]);

        // หน้าแรกไม่มีปุ่มถัดไป (เลือกเพศแล้วไปต่อเอง)
        nextBtn.setVisible(step != 0);
        nextBtn.setText(step == 3 ? "พร้อมแล้ว!" : step == 2 ? "ดูผลการคำนวณ" : "ถัดไป");
        backBtn.setVisible(true);
        actionRow.removeAll();
        actionColumn.removeAll();
        if (step == 1) {
            actionColumn.add(nextBtn);
            actionColumn.add(Box.createVerticalStrut(10));
            actionColumn.add(backBtn);
        } else {
            actionRow.add(backBtn);
            actionRow.add(nextBtn);
        }
        actionColumn.setVisible(step == 1);
        actionRow.setVisible(step != 1);
        if (step == 2) {
            styleOutlinedNextButton();
            styleArrowBackButton();
        } else {
            styleFilledNextButton();
            styleFilledBackButton();
            if (step == 0) {
                backBtn.setPreferredSize(new Dimension(240, 42));
                backBtn.setMinimumSize(new Dimension(240, 42));
                backBtn.setMaximumSize(new Dimension(240, 42));
            } else {
                backBtn.setPreferredSize(new Dimension(240, 42));
                backBtn.setMinimumSize(new Dimension(240, 42));
                backBtn.setMaximumSize(new Dimension(240, 42));
            }
        }

        if (step == 1) {
            bodyAvatar.setIcon(null);
            ImageIcon icon = createCharacterAvatar(isMale);
            if (icon != null) {
                bodyAvatar.setIcon(icon);
                bodyAvatar.setText("");
            } else {
                bodyAvatar.setText(isMale ? "👦" : "👧");
                bodyAvatar.setFont(new Font("Dialog", Font.PLAIN, 90));
            }
        }

        // จุดบอกหน้า
        dotsPanel.removeAll();
        for (int i = 0; i < 3; i++) {
            final boolean active = i == Math.min(step, 2);
            JComponent dot = new JComponent() {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(active ? new Color(0xF0, 0xB0, 0x50) : new Color(0xD8, 0xD8, 0xD8));
                    g.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                    g.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(active ? 12 : 8, active ? 12 : 8));
            dot.setMinimumSize(dot.getPreferredSize());
            dot.setMaximumSize(dot.getPreferredSize());
            dotsPanel.add(dot);
        }
        dotsPanel.revalidate();
        dotsPanel.repaint();
        actionRow.revalidate();
        actionColumn.revalidate();
        revalidate();
        repaint();
    }

    private void highlightGender() {
        femaleBtn.setBackground(!isMale ? SELECTED : Color.WHITE);
        maleBtn.setBackground(isMale ? SELECTED : Color.WHITE);
    }

    private JButton makeGenderButton(String iconPath, Color color) {
        JButton b = new JButton(loadIcon(iconPath, 92)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 38, 38));
                g.setColor(new Color(0x1F, 0x1F, 0x1F));
                g.setStroke(new BasicStroke(1.2f));
                g.draw(new RoundRectangle2D.Double(1.6, 1.6, getWidth() - 4.2, getHeight() - 4.2, 38, 38));
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(145, 135));
        b.setMinimumSize(new Dimension(145, 135));
        b.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        b.getAccessibleContext().setAccessibleName(color.getBlue() > color.getRed()
                ? "เลือกเพศชาย" : "เลือกเพศหญิง");
        return b;
    }

    private JButton makeRoundButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_TEXT);
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(240, 42));
        b.setMaximumSize(new Dimension(240, 42));
        b.setMinimumSize(new Dimension(240, 42));
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton button = (JButton) component;
                Color color = button.getModel().isPressed()
                        ? bg.darker() : button.getModel().isRollover() ? bg.brighter() : bg;
                g.setColor(color);
                g.fill(new RoundRectangle2D.Double(0, 0, button.getWidth(), button.getHeight(),
                        button.getHeight(), button.getHeight()));
                g.dispose();
                super.paint(graphics, component);
            }
        });
        return b;
    }

    private void styleFilledNextButton() {
        nextBtn.setFont(FONT_TEXT);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setPreferredSize(new Dimension(240, 42));
        nextBtn.setMinimumSize(new Dimension(240, 42));
        nextBtn.setMaximumSize(new Dimension(240, 42));
        nextBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton button = (JButton) component;
                Color color = button.getModel().isPressed()
                        ? PURPLE.darker() : button.getModel().isRollover() ? PURPLE.brighter() : PURPLE;
                g.setColor(color);
                g.fill(new RoundRectangle2D.Double(0, 0, button.getWidth(), button.getHeight(),
                        button.getHeight(), button.getHeight()));
                g.dispose();
                super.paint(graphics, component);
            }
        });
    }

    private void styleOutlinedNextButton() {
        nextBtn.setFont(new Font("Tahoma", Font.PLAIN, 25));
        nextBtn.setForeground(new Color(0x43, 0x9B, 0xFF));
        nextBtn.setPreferredSize(new Dimension(264, 54));
        nextBtn.setMinimumSize(new Dimension(264, 54));
        nextBtn.setMaximumSize(new Dimension(264, 54));
        nextBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Double(1, 1, component.getWidth() - 2,
                        component.getHeight() - 2, component.getHeight(), component.getHeight()));
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1.1f));
                g.draw(new RoundRectangle2D.Double(1, 1, component.getWidth() - 3,
                        component.getHeight() - 3, component.getHeight(), component.getHeight()));
                g.dispose();
                super.paint(graphics, component);
            }
        });
    }

    private void styleFilledBackButton() {
        backBtn.setText("ย้อนกลับ");
        backBtn.setFont(FONT_TEXT);
        backBtn.setForeground(Color.WHITE);
        backBtn.setPreferredSize(new Dimension(240, 42));
        backBtn.setMinimumSize(new Dimension(240, 42));
        backBtn.setMaximumSize(new Dimension(240, 42));
        backBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton button = (JButton) component;
                Color color = button.getModel().isPressed()
                        ? PINK.darker() : button.getModel().isRollover() ? PINK.brighter() : PINK;
                g.setColor(color);
                g.fill(new RoundRectangle2D.Double(0, 0, button.getWidth(), button.getHeight(),
                        button.getHeight(), button.getHeight()));
                g.dispose();
                super.paint(graphics, component);
            }
        });
    }

    private void styleArrowBackButton() {
        backBtn.setText("←");
        backBtn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 26));
        backBtn.setForeground(new Color(0x43, 0x9B, 0xFF));
        backBtn.setPreferredSize(new Dimension(44, 44));
        backBtn.setMinimumSize(new Dimension(44, 44));
        backBtn.setMaximumSize(new Dimension(44, 44));
        backBtn.getAccessibleContext().setAccessibleName("ย้อนกลับ");
        backBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);
                g.fillOval(1, 1, component.getWidth() - 2, component.getHeight() - 2);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1.1f));
                g.drawOval(1, 1, component.getWidth() - 3, component.getHeight() - 3);
                g.dispose();
                super.paint(graphics, component);
            }
        });
    }

    private JLabel makeAvatar(String path, String fallback, int height) {
        ImageIcon icon = loadIcon(path, height);
        JLabel label = (icon != null) ? new JLabel(icon) : new JLabel(fallback, SwingConstants.CENTER);
        if (icon == null) {
            label.setFont(new Font("Dialog", Font.PLAIN, 80));
        }
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /** โหลดรูปจาก classpath แล้วย่อให้สูงตามที่กำหนด (ไม่มีไฟล์ = null) */
    private ImageIcon loadIcon(String path, int height) {
        BufferedImage image = loadImage(path);
        if (image == null) return null;

        int width = image.getWidth() * height / Math.max(1, image.getHeight());
        return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    private ImageIcon createCharacterAvatar(boolean male) {
        BufferedImage source = loadImage("/healthycalories/app/ui/screen/images/Welcome.png");
        if (source == null) return null;

        int split = source.getWidth() / 2;
        BufferedImage character = male
                ? source.getSubimage(split, 0, source.getWidth() - split, source.getHeight())
                : source.getSubimage(0, 0, split, source.getHeight());

        int size = 270;
        BufferedImage avatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = avatar.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xE8, 0xE8, 0xE8));
        g.drawOval(8, 8, size - 16, size - 16);

        double scale = Math.min((double) (size - 28) / character.getWidth(),
                (double) (size - 16) / character.getHeight());
        int width = (int) Math.round(character.getWidth() * scale);
        int height = (int) Math.round(character.getHeight() * scale);
        int x = (size - width) / 2;
        int y = size - height;
        g.drawImage(character, x, y, width, height, null);
        g.dispose();
        return new ImageIcon(avatar);
    }

    private BufferedImage loadImage(String path) {
        URL url = getClass().getResource(path);
        try {
            BufferedImage image = url == null ? null : ImageIO.read(url);
            if (image == null) {
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                File photoFile = new File("ProjectFolders" + File.separator + "Photo", fileName);
                if (photoFile.isFile()) {
                    image = ImageIO.read(photoFile);
                }
            }
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("อ่านรูปภาพ Onboarding ไม่สำเร็จ: " + path, e);
        }
    }

    private void addFormRow(JPanel form, int row, String label, JTextField field, String unit) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(8, 6, 8, 6);

        JLabel l = new JLabel(label);
        l.setFont(FONT_TEXT);
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        form.add(l, gc);

        field.setFont(FONT_TEXT);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBackground(BG);
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        form.add(field, gc);

        JLabel u = new JLabel(unit);
        u.setFont(FONT_TEXT);
        gc.gridx = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        form.add(u, gc);
    }
}