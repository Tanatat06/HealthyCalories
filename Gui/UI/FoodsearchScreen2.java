package UI;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

public class FoodsearchScreen2 extends JFrame {

    // โหลดรูปจากโฟลเดอร์ image (ลองหาใน classpath ก่อน แล้วค่อยหาจากไฟล์)
    private static ImageIcon loadIcon(String name, int w, int h) {
        Image img = null;
        URL url = FoodsearchScreen2.class.getResource("/images/" + name);
        if (url != null) {
            img = new ImageIcon(url).getImage();
        } else {
            File f = new File("images/" + name);
            if (f.exists()) {
                img = new ImageIcon(f.getPath()).getImage();
            } else {
                System.out.println("ไม่พบรูป: " + f.getAbsolutePath());
            }
        }
        if (img == null) {
            return null;
        }
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    public FoodsearchScreen2() {
        setTitle("FoodsearchScreen2");
        setSize(400, 860);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(null);
        root.setBackground(Color.WHITE);
        setContentPane(root);

        JPanel header = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(214, 170, 230));
                g2.fillRoundRect(60, 100, 340, 90, 80, 80);
                g2.setColor(new Color(224, 190, 238));
                g2.fillRoundRect(-40, -60, 480, 200, 80, 80);
                g2.fillRoundRect(20, 80, 380, 90, 80, 80);
            }
        };
        header.setBounds(0, 0, 400, 180);
        header.setOpaque(false);
        root.add(header);

        JLabel back = new JLabel("\u2190");
        back.setFont(new Font("Tahoma", Font.PLAIN, 34));
        back.setBounds(15, 5, 50, 45);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.add(back);

        JLabel title = new JLabel("อาหารเย็น", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.PLAIN, 20));
        title.setBounds(100, 10, 200, 35);
        header.add(title);

        JLabel today = new JLabel("วันนี้", SwingConstants.RIGHT);
        today.setFont(new Font("Tahoma", Font.PLAIN, 14));
        today.setBounds(300, 15, 80, 25);
        header.add(today);

        JPanel searchBox = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);
                g2.setColor(new Color(200, 170, 215));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setBounds(25, 55, 350, 48);
        header.add(searchBox);

        JLabel searchIcon = new JLabel("\uD83D\uDD0D");
        searchIcon.setFont(new Font("Dialog", Font.PLAIN, 18));
        searchIcon.setBounds(15, 8, 30, 30);
        searchBox.add(searchIcon);

        final String hint = "ใส่ชื่อที่ต้องการค้นหา...";
        final JTextField searchField = new JTextField(hint);
        searchField.setFont(new Font("Tahoma", Font.PLAIN, 14));
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setBounds(50, 8, 285, 32);
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(hint)) {
                    searchField.setText("");
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(hint);
                }
            }
        });
        searchBox.add(searchField);

        java.util.Map<java.awt.font.TextAttribute, Object> underline = new java.util.HashMap<java.awt.font.TextAttribute, Object>();
        underline.put(java.awt.font.TextAttribute.UNDERLINE, java.awt.font.TextAttribute.UNDERLINE_ON);
        JLabel tabSearch = new JLabel("ค้นหา", SwingConstants.CENTER);
        tabSearch.setFont(new Font("Tahoma", Font.PLAIN, 18).deriveFont(underline));
        tabSearch.setBounds(40, 115, 90, 35);
        header.add(tabSearch);

        JLabel tabMine = new JLabel("รายการของฉัน", SwingConstants.CENTER);
        tabMine.setFont(new Font("Tahoma", Font.PLAIN, 18));
        tabMine.setBounds(255, 115, 140, 35);
        tabMine.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tabMine.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new FoodsearchScreen1().setVisible(true);
                dispose();
            }
        });
        header.add(tabMine);

        // ปุ่ม "ค้นหาจากหมวดหมู่"
        JButton categoryBtn = new JButton("ค้นหาจากหมวดหมู่", loadIcon("food.png", 55, 40)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(235, 200, 238));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(246, 220, 246));
                } else {
                    g2.setColor(new Color(252, 236, 252));
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        categoryBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
        categoryBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        categoryBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        categoryBtn.setIconTextGap(4);
        categoryBtn.setMargin(new Insets(0, 0, 0, 0));
        categoryBtn.setContentAreaFilled(false);
        categoryBtn.setBorderPainted(false);
        categoryBtn.setFocusPainted(false);
        categoryBtn.setOpaque(false);
        categoryBtn.setRolloverEnabled(true);
        categoryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        categoryBtn.setBounds(145, 105, 110, 80);
        categoryBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: ใส่โค้ดเปิดหน้าหมวดหมู่ตรงนี้
            }
        });
        header.add(categoryBtn);

        JLabel emptyText = new JLabel("ยังไม่มีรายการล่าสุดในขณะนี้", SwingConstants.CENTER);
        emptyText.setFont(new Font("Tahoma", Font.PLAIN, 20));
        emptyText.setBounds(30, 410, 340, 35);
        root.add(emptyText);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FoodsearchScreen2().setVisible(true);
            }
        });
    }
}