package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.User;
import com.tolet.model.User.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;

/** Welcome screen with login and registration. */
public class LoginFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel right = new JPanel(cards);

    public LoginFrame() {
        super("ToLet Hub - Rent & Buy Homes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(hero());
        right.setBackground(Ui.SURFACE);
        right.add(loginForm(), "login"); right.add(registerForm(), "register");
        root.add(right);
        setContentPane(root);
        setSize(1020, 640); setMinimumSize(new Dimension(900, 600)); setLocationRelativeTo(null);
    }

    private JComponent hero() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g.setPaint(new GradientPaint(0, 0, new Color(49, 46, 129), w, h, new Color(124, 58, 237)));
                g.fillRect(0, 0, w, h);
                g.setColor(new Color(255, 255, 255, 24));
                g.fillOval(-80, h - 260, 340, 340); g.fillOval(w - 220, -90, 330, 330);
                // skyline
                int[] hs = { 90, 150, 110, 190, 130, 170, 100, 140, 120 };
                int bw = w / hs.length;
                for (int i = 0; i < hs.length; i++) {
                    g.setColor(new Color(255, 255, 255, 38));
                    g.fillRoundRect(i * bw + 6, h - hs[i], bw - 10, hs[i] + 10, 8, 8);
                    g.setColor(new Color(253, 224, 71, 150));
                    for (int r = 0; r < hs[i] / 28; r++) { g.fillRect(i * bw + 16, h - hs[i] + 14 + r * 26, 9, 11); g.fillRect(i * bw + bw - 28, h - hs[i] + 14 + r * 26, 9, 11); }
                }
                g.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(60, 48, 40, 40));
        JLabel logo = Ui.label("ToLet Hub", Font.BOLD, 42, Color.WHITE);
        JLabel tag = Ui.label("Find it. Love it. Live in it.", Font.PLAIN, 19, new Color(224, 231, 255));
        p.add(logo); p.add(Box.createVerticalStrut(6)); p.add(tag); p.add(Box.createVerticalStrut(32));
        String[] feats = { "Rent or buy - thousands of homes, one place", "Smart Match picks homes that fit your taste", "Side-by-side comparison of properties",
                "EMI, rent-vs-buy and affordability calculators", "Schedule visits and send offers in a click", "Instant rent agreements and receipts" };
        for (String f : feats) { JLabel l = Ui.label("\u2713  " + f, Font.PLAIN, 14, Color.WHITE); l.setBorder(new EmptyBorder(5, 0, 5, 0)); p.add(l); }
        return p;
    }

    private JComponent loginForm() {
        JPanel p = new JPanel(); p.setBackground(Ui.SURFACE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(70, 70, 40, 70));
        JTextField email = Ui.field(20); JPasswordField pw = Ui.pass(20);
        JLabel err = Ui.label(" ", Font.PLAIN, 12, Ui.DANGER);
        Ui.RButton go = Ui.primary("Sign In");
        Runnable login = () -> {
            User u = DataStore.get().login(email.getText(), new String(pw.getPassword()));
            if (u == null) { err.setText("Wrong email or password."); return; }
            if (u.blocked) { err.setText("This account has been blocked by the admin."); return; }
            Session.user = u; Session.main = new MainFrame(); Session.main.setVisible(true); dispose();
        };
        go.addActionListener(e -> login.run()); pw.addActionListener(e -> login.run()); email.addActionListener(e -> pw.requestFocus());
        add(p, Ui.label("Welcome back", Font.BOLD, 28, Ui.TEXT)); add(p, Ui.muted("Sign in to continue"));
        p.add(Box.createVerticalStrut(24));
        add(p, Ui.label("Email", Font.BOLD, 13, Ui.TEXT)); add(p, email); p.add(Box.createVerticalStrut(12));
        add(p, Ui.label("Password", Font.BOLD, 13, Ui.TEXT)); add(p, pw); p.add(Box.createVerticalStrut(6)); add(p, err);
        p.add(Box.createVerticalStrut(8)); add(p, go); p.add(Box.createVerticalStrut(14));
        Ui.RButton reg = Ui.ghost("Create a new account"); reg.setForeground(Ui.TEXT);
        reg.addActionListener(e -> cards.show(right, "register")); add(p, reg);
        p.add(Box.createVerticalStrut(20));
        JLabel demo = Ui.label("<html><b>Demo logins</b><br>Customer: user@tolet.com / user123<br>Owner: owner@tolet.com / owner123<br>Admin: admin@tolet.com / admin123</html>", Font.PLAIN, 12, Ui.MUTED);
        add(p, demo);
        return p;
    }

    private JComponent registerForm() {
        JPanel p = new JPanel(); p.setBackground(Ui.SURFACE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(40, 70, 30, 70));
        JTextField name = Ui.field(20), email = Ui.field(20), phone = Ui.field(20);
        JPasswordField pw = Ui.pass(20), pw2 = Ui.pass(20);
        JComboBox<String> role = Ui.combo(new String[] { "Customer - I want to rent or buy", "Owner - I want to list properties" });
        JLabel err = Ui.label(" ", Font.PLAIN, 12, Ui.DANGER);
        Ui.RButton go = Ui.success("Create Account");
        go.addActionListener(e -> {
            if (!new String(pw.getPassword()).equals(new String(pw2.getPassword()))) { err.setText("Passwords do not match."); return; }
            try {
                User u = DataStore.get().register(name.getText(), email.getText(), phone.getText(), new String(pw.getPassword()),
                        role.getSelectedIndex() == 0 ? Role.CUSTOMER : Role.OWNER);
                Session.user = u; Session.main = new MainFrame(); Session.main.setVisible(true); dispose();
            } catch (IllegalArgumentException ex) { err.setText(ex.getMessage()); }
        });
        add(p, Ui.label("Create your account", Font.BOLD, 26, Ui.TEXT)); p.add(Box.createVerticalStrut(14));
        String[] labels = { "Full name", "Email", "Phone", "Password (min 6 characters)", "Confirm password", "I am a" };
        JComponent[] fields = { name, email, phone, pw, pw2, role };
        for (int i = 0; i < labels.length; i++) { add(p, Ui.label(labels[i], Font.BOLD, 13, Ui.TEXT)); add(p, fields[i]); p.add(Box.createVerticalStrut(8)); }
        add(p, err); add(p, go); p.add(Box.createVerticalStrut(10));
        Ui.RButton back = Ui.ghost("Back to sign in"); back.setForeground(Ui.TEXT);
        back.addActionListener(e -> cards.show(right, "login")); add(p, back);
        return p;
    }

    private static void add(JPanel p, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        if (c instanceof JTextField || c instanceof JComboBox || c instanceof JButton) c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        p.add(c);
    }
}
