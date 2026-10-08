package main.java.healthycalories.app.ui.screen;

import javax.swing.*;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

import java.awt.*;

public class OnboardingScreen extends BaseScreen {

    // ---------- สี / ฟอนต์ ----------
    private static final Color BG = new Color(0xFF, 0xFB, 0xEF);
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
    private JPanel dotsPanel;

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

        dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        dotsPanel.setOpaque(false);
        bottom.add(dotsPanel);
        bottom.add(Box.createVerticalStrut(15));

        nextBtn = makeRoundButton("ถัดไป", PURPLE);
        backBtn = makeRoundButton("ย้อนกลับ", PINK);
        nextBtn.addActionListener(e -> next());
        backBtn.addActionListener(e -> back());

        nextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottom.add(nextBtn);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(backBtn);

        add(bottom, BorderLayout.SOUTH);

        updateStepUI();
    }

    /** หน้า 1: เลือกเพศ */
    private JPanel buildGenderPage() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(40, 20, 10, 20));

        JLabel title = new JLabel("ยินดีต้อนรับสู่ HealthyCalories !");
        title.setFont(FONT_TITLE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("กรุณาบอกเราเกี่ยวกับตัวคุณสักนิด");
        sub.setFont(FONT_SMALL);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // รูปตัวการ์ตูน (ใส่ไฟล์ไว้ที่ resources/images/)
        JPanel avatars = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        avatars.setOpaque(false);
        avatars.add(makeAvatar("/images/girl.png", "👧"));
        avatars.add(makeAvatar("/images/boy.png", "👦"));

        // ปุ่มเลือกเพศ
        femaleBtn = makeGenderButton("♀", new Color(0xE8, 0x7A, 0x90));
        maleBtn = makeGenderButton("♂", new Color(0x6F, 0x8F, 0xD8));
        femaleBtn.addActionListener(e -> {
            isMale = false;
            chooseGender();
        });
        maleBtn.addActionListener(e -> {
            isMale = true;
            chooseGender();
        });

        JPanel genderRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 0));
        genderRow.setOpaque(false);
        genderRow.add(femaleBtn);
        genderRow.add(maleBtn);

        p.add(title);
        p.add(Box.createVerticalStrut(5));
        p.add(sub);
        p.add(Box.createVerticalStrut(25));
        p.add(avatars);
        p.add(Box.createVerticalStrut(25));
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
        bodyAvatar.setPreferredSize(new Dimension(200, 200));

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
        // แล้วเปลี่ยนไปหน้าหลัก เช่น frame.showScreen("home");
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
        backBtn.setVisible(step != 0);

        if (step == 1) {
            bodyAvatar.setIcon(null);
            ImageIcon icon = loadIcon(isMale ? "/images/boy.png" : "/images/girl.png", 180);
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
            JLabel dot = new JLabel("●");
            dot.setForeground(i == Math.min(step, 2) ? new Color(0xF0, 0xB0, 0x50) : Color.LIGHT_GRAY);
            dotsPanel.add(dot);
        }
        dotsPanel.revalidate();
        dotsPanel.repaint();
    }

    private void highlightGender() {
        femaleBtn.setBackground(!isMale ? SELECTED : Color.WHITE);
        maleBtn.setBackground(isMale ? SELECTED : Color.WHITE);
    }

    private JButton makeGenderButton(String symbol, Color color) {
        JButton b = new JButton(symbol);
        b.setFont(new Font("Dialog", Font.BOLD, 40));
        b.setForeground(color);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(90, 90));
        b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true));
        return b;
    }

    private JButton makeRoundButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_TEXT);
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(240, 42));
        b.setMaximumSize(new Dimension(240, 42));
        return b;
    }

    private JLabel makeAvatar(String path, String fallback) {
        ImageIcon icon = loadIcon(path, 150);
        JLabel l = (icon != null) ? new JLabel(icon) : new JLabel(fallback, SwingConstants.CENTER);
        if (icon == null) {
            l.setFont(new Font("Dialog", Font.PLAIN, 80));
        }
        return l;
    }

    /** โหลดรูปจาก classpath แล้วย่อให้สูงตามที่กำหนด (ไม่มีไฟล์ = null) */
    private ImageIcon loadIcon(String path, int height) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) return null;
        ImageIcon raw = new ImageIcon(url);
        int w = raw.getIconWidth() * height / Math.max(1, raw.getIconHeight());
        return new ImageIcon(raw.getImage().getScaledInstance(w, height, Image.SCALE_SMOOTH));
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