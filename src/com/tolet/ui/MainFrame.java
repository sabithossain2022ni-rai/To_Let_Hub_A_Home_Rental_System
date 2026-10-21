package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.Notification;
import com.tolet.model.User;
import com.tolet.model.User.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/** Main application window: sidebar navigation, header and page container. */
public class MainFrame extends JFrame {
    private final CardLayout cl = new CardLayout();
    private final JPanel pages = new JPanel(cl);
    private final Map<String, Refreshable> registry = new HashMap<>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final JLabel pageTitle = Ui.label("", Font.BOLD, 16, Ui.TEXT);
    private final JButton bell = new JButton();
    private BrowsePanel browse;

    public MainFrame() {
        super("ToLet Hub");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        User u = Session.user;
        pages.setBackground(Ui.BG); pages.setBorder(new EmptyBorder(18, 22, 10, 14));

        browse = new BrowsePanel();
        add("home", "Dashboard", new DashboardPanel());
        add("browse", "Browse Homes", browse);
        if (u.role == Role.CUSTOMER) { add("fav", "Favorites", new FavoritesPanel()); add("requests", "My Requests", new BookingsPanel()); }
        if (u.role == Role.OWNER) { add("listings", "My Listings", new ListingsPanel()); add("requests", "Incoming Requests", new BookingsPanel()); }
        if (u.role == Role.ADMIN) { add("admin", "Admin Console", new AdminPanel()); add("listings", "All Listings", new ListingsPanel()); add("requests", "All Bookings", new BookingsPanel()); }
        add("calc", "Calculators", new CalculatorPanel());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.BG);
        root.add(sidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(Ui.BG);
        main.add(header(), BorderLayout.NORTH); main.add(pages, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        setSize(1280, 800); setMinimumSize(new Dimension(1050, 680)); setLocationRelativeTo(null);
        show("home");
    }

    private void add(String key, String title, JPanel page) {
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(page);
        pages.add(wrap, key);
        registry.put(key, (Refreshable) page);
        JButton b = new JButton("   " + title) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = Boolean.TRUE.equals(getClientProperty("sel"));
                if (sel) { g.setColor(new Color(255, 255, 255, 40)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g.setColor(Ui.ACCENT); g.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4); }
                else if (getModel().isRollover()) { g.setColor(new Color(255, 255, 255, 18)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14); }
                g.dispose(); super.paintComponent(g0);
            }
        };
        b.setForeground(Color.WHITE); b.setFont(Ui.font(Font.PLAIN, 14)); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); b.setPreferredSize(new Dimension(200, 42));
        b.putClientProperty("title", title);
        b.addActionListener(e -> show(key));
        navButtons.put(key, b);
    }

    private JComponent sidebar() {
        JPanel s = new JPanel();
        s.setBackground(Ui.SIDEBAR); s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBorder(new EmptyBorder(22, 14, 18, 14)); s.setPreferredSize(new Dimension(230, 0));
        JLabel logo = Ui.label("ToLet Hub", Font.BOLD, 24, Color.WHITE); logo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel tag = Ui.label("Rent & Buy Homes", Font.PLAIN, 12, new Color(165, 180, 252)); tag.setAlignmentX(LEFT_ALIGNMENT);
        s.add(logo); s.add(tag); s.add(Box.createVerticalStrut(26));
        for (JButton b : navButtons.values()) { b.setAlignmentX(LEFT_ALIGNMENT); s.add(b); s.add(Box.createVerticalStrut(4)); }
        s.add(Box.createVerticalGlue());
        User u = Session.user;
        JLabel n = Ui.label(u.name, Font.BOLD, 14, Color.WHITE), r = Ui.label(u.role.label() + "  |  " + u.email, Font.PLAIN, 11, new Color(165, 180, 252));
        n.setAlignmentX(LEFT_ALIGNMENT); r.setAlignmentX(LEFT_ALIGNMENT);
        s.add(n); s.add(r);
        return s;
    }

    private JComponent header() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Ui.SURFACE); h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDER), new EmptyBorder(10, 22, 10, 16)));
        h.add(pageTitle, BorderLayout.WEST);
        JPanel r = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bell.setFont(Ui.font(Font.BOLD, 13)); bell.setForeground(Ui.TEXT);
        bell.setContentAreaFilled(false); bell.setFocusPainted(false); bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bell.setBorder(BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(Ui.BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        bell.addActionListener(e -> showNotes());
        Ui.RButton theme = Ui.ghost(Ui.dark ? "Light Mode" : "Dark Mode"); theme.setForeground(Ui.TEXT);
        theme.addActionListener(e -> { Ui.apply(!Ui.dark); MainFrame f = new MainFrame(); Session.main = f; f.setBounds(getBounds()); f.setVisible(true); dispose(); });
        Ui.RButton out = Ui.danger("Logout"); out.addActionListener(e -> logout());
        r.add(bell); r.add(theme); r.add(out);
        h.add(r, BorderLayout.EAST);
        updateBell();
        return h;
    }

    private void updateBell() {
        long n = DataStore.get().unread(Session.user.id);
        bell.setText("Notifications" + (n > 0 ? "  (" + n + ")" : ""));
    }

    private void showNotes() {
        DataStore db = DataStore.get();
        List<Notification> list = db.notes(Session.user.id);
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Ui.SURFACE); p.setBorder(new EmptyBorder(10, 10, 10, 10));
        if (list.isEmpty()) p.add(Ui.muted("You're all caught up."));
        for (Notification n : list) {
            Ui.RPanel c = new Ui.RPanel(new BorderLayout(), 12); c.setBorder(new EmptyBorder(8, 12, 8, 12));
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64)); c.setAlignmentX(LEFT_ALIGNMENT);
            c.add(Ui.label((n.read ? "" : "\u25CF ") + n.text, n.read ? Font.PLAIN : Font.BOLD, 13, Ui.TEXT), BorderLayout.CENTER);
            c.add(Ui.label(Ui.time(n.time), Font.PLAIN, 11, Ui.MUTED), BorderLayout.SOUTH);
            p.add(c); p.add(Box.createVerticalStrut(6));
        }
        JScrollPane sp = new JScrollPane(p); sp.setPreferredSize(new Dimension(480, 360)); sp.setBorder(null);
        JOptionPane.showMessageDialog(this, sp, "Notifications", JOptionPane.PLAIN_MESSAGE);
        db.markRead(Session.user.id); updateBell();
    }

    public void show(String key) {
        Refreshable r = registry.get(key);
        if (r != null) r.refresh();
        cl.show(pages, key);
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            e.getValue().putClientProperty("sel", e.getKey().equals(key)); e.getValue().repaint();
            if (e.getKey().equals(key)) pageTitle.setText((String) e.getValue().getClientProperty("title"));
        }
        updateBell();
    }

    public void browse(int mode) { browse.presetMode(mode); show("browse"); }

    public void logout() {
        Session.user = null; Session.main = null;
        dispose();
        new LoginFrame().setVisible(true);
    }
}
