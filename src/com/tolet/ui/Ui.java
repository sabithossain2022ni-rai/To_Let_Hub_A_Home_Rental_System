package com.tolet.ui;

import com.tolet.model.Property;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Theme colours and small reusable, custom-painted widgets. */
public final class Ui {
    private Ui() {}

    public static boolean dark = false;
    public static Color BG, SURFACE, TEXT, MUTED, BORDER, SIDEBAR;
    public static final Color PRIMARY = new Color(79, 70, 229);
    public static final Color PRIMARY_DARK = new Color(55, 48, 163);
    public static final Color ACCENT = new Color(16, 185, 129);
    public static final Color WARN = new Color(245, 158, 11);
    public static final Color DANGER = new Color(239, 68, 68);

    static { apply(false); }

    public static void apply(boolean d) {
        dark = d;
        if (d) {
            BG = new Color(15, 23, 42); SURFACE = new Color(30, 41, 59); TEXT = new Color(241, 245, 249);
            MUTED = new Color(148, 163, 184); BORDER = new Color(51, 65, 85); SIDEBAR = new Color(2, 6, 23);
        } else {
            BG = new Color(243, 244, 248); SURFACE = Color.WHITE; TEXT = new Color(17, 24, 39);
            MUTED = new Color(107, 114, 128); BORDER = new Color(229, 231, 235); SIDEBAR = new Color(30, 27, 75);
        }
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", font(Font.PLAIN, 13));
        UIManager.put("Button.font", font(Font.PLAIN, 13));
        UIManager.put("TabbedPane.background", SURFACE);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.contentAreaColor", SURFACE);
        UIManager.put("TabbedPane.selected", SURFACE);
        UIManager.put("TabbedPane.light", BORDER);
        UIManager.put("TabbedPane.highlight", BORDER);
        UIManager.put("TabbedPane.shadow", BORDER);
        UIManager.put("TabbedPane.darkShadow", BORDER);
        UIManager.put("TabbedPane.focus", PRIMARY);
        UIManager.put("TabbedPane.font", font(Font.BOLD, 13));
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("CheckBox.background", SURFACE);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", BG);
        UIManager.put("Spinner.background", SURFACE);
        UIManager.put("Spinner.foreground", TEXT);
        UIManager.put("FormattedTextField.background", SURFACE);
        UIManager.put("FormattedTextField.foreground", TEXT);
        UIManager.put("Slider.background", SURFACE);
        UIManager.put("Slider.foreground", PRIMARY);
        UIManager.put("List.background", SURFACE);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("ToolTip.font", font(Font.PLAIN, 12));
    }

    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }

    // ----------------------------------------------------------------- format
    public static String money(double v) { return "BDT " + NumberFormat.getIntegerInstance(Locale.US).format(Math.round(v)); }
    public static String priceOf(Property p) {
        return p.mode == Property.Mode.RENT ? money(p.price) + " / mo" : money(p.price);
    }
    public static String shortMoney(double v) {
        if (v >= 10000000) return String.format("%.2f Cr", v / 10000000);
        if (v >= 100000) return String.format("%.2f Lac", v / 100000);
        return NumberFormat.getIntegerInstance(Locale.US).format(Math.round(v));
    }
    public static String time(long t) { return new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new Date(t)); }
    public static String day(long t) { return new SimpleDateFormat("dd MMM yyyy").format(new Date(t)); }
    public static String stars(double r) {
        int full = (int) Math.round(r);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < full ? "\u2605" : "\u2606");
        return sb.toString();
    }

    // ------------------------------------------------------------ small helpers
    public static JLabel label(String t, int style, int size, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(font(style, size)); l.setForeground(c);
        return l;
    }
    public static JLabel title(String t) { return label(t, Font.BOLD, 24, TEXT); }
    public static JLabel muted(String t) { return label(t, Font.PLAIN, 13, MUTED); }

    public static void styleInput(JComponent c) {
        c.setFont(font(Font.PLAIN, 14));
        c.setBackground(SURFACE); c.setForeground(TEXT);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        styleInput(f); f.setCaretColor(TEXT);
        return f;
    }
    public static JPasswordField pass(int cols) {
        JPasswordField f = new JPasswordField(cols);
        styleInput(f); f.setCaretColor(TEXT);
        return f;
    }
    public static JTextArea area(int rows, int cols) {
        JTextArea a = new JTextArea(rows, cols);
        styleInput(a); a.setCaretColor(TEXT); a.setLineWrap(true); a.setWrapStyleWord(true);
        return a;
    }
    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(font(Font.PLAIN, 13)); c.setBackground(SURFACE); c.setForeground(TEXT);
        return c;
    }
    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(null); s.getViewport().setBackground(BG);
        s.getVerticalScrollBar().setUnitIncrement(18);
        return s;
    }

    public static void msg(Component parent, String m) {
        JOptionPane.showMessageDialog(parent, m, "ToLet Hub", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void error(Component parent, String m) {
        JOptionPane.showMessageDialog(parent, m, "ToLet Hub", JOptionPane.WARNING_MESSAGE);
    }
    public static boolean confirm(Component parent, String m) {
        return JOptionPane.showConfirmDialog(parent, m, "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Adds "label : component" rows to a GridBag form. */
    public static void row(JPanel form, int y, String label, Component c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y; g.insets = new Insets(5, 4, 5, 8); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; form.add(label(label, Font.BOLD, 13, TEXT), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; form.add(c, g);
    }

    public static void styleTable(JTable t) {
        t.setRowHeight(34); t.setFont(font(Font.PLAIN, 13));
        t.setBackground(SURFACE); t.setForeground(TEXT);
        t.setGridColor(BORDER); t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 200));
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 36));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, f, r, c);
                l.setBackground(PRIMARY); l.setForeground(Color.WHITE);
                l.setFont(font(Font.BOLD, 13)); l.setBorder(new EmptyBorder(0, 10, 0, 10));
                return l;
            }
        });
        DefaultTableCellRenderer pad = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, f, r, c);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                String txt = String.valueOf(v);
                if (!s) {
                    l.setForeground(TEXT);
                    switch (txt) {
                        case "Approved": case "Available": l.setForeground(ACCENT.darker()); break;
                        case "Pending": l.setForeground(WARN.darker()); break;
                        case "Rejected": case "Cancelled": case "Blocked": l.setForeground(DANGER); break;
                        case "Closed": l.setForeground(MUTED); break;
                        default: break;
                    }
                }
                return l;
            }
        };
        t.setDefaultRenderer(Object.class, pad);
    }

    public static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }

    /** Panel that always matches the scroll pane width (no horizontal scrolling). */
    public static class VPanel extends JPanel implements Scrollable {
        public VPanel(LayoutManager lm) { super(lm); setOpaque(false); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 18; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ---------------------------------------------------------------- buttons
    public static class RButton extends JButton {
        private final Color base; private final boolean outline; private boolean hover;
        RButton(String t, Color base, Color fg, boolean outline) {
            super(t);
            this.base = base; this.outline = outline;
            setForeground(fg); setFont(font(Font.BOLD, 13));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (outline) {
                g.setColor(hover ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 30) : new Color(0, 0, 0, 0));
                g.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g.setColor(isEnabled() ? base : BORDER);
                g.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            } else {
                g.setColor(!isEnabled() ? new Color(160, 165, 175) : hover ? base.darker() : base);
                g.fillRoundRect(0, 0, w, h, 12, 12);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }
    public static RButton primary(String t) { return new RButton(t, PRIMARY, Color.WHITE, false); }
    public static RButton success(String t) { return new RButton(t, ACCENT, Color.WHITE, false); }
    public static RButton danger(String t) { return new RButton(t, DANGER, Color.WHITE, false); }
    public static RButton ghost(String t) { return new RButton(t, PRIMARY, TEXT, true); }

    // ----------------------------------------------------------------- panels
    public static class RPanel extends JPanel {
        private final int arc;
        public RPanel(LayoutManager lm, int arc) { super(lm); this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(SURFACE);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
            g.setColor(BORDER);
            g.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    public static JPanel transparent(LayoutManager lm) { JPanel p = new JPanel(lm); p.setOpaque(false); return p; }

    public static class Badge extends JLabel {
        private final Color bg;
        public Badge(String text, Color bg) {
            super(text);
            this.bg = bg;
            setFont(font(Font.BOLD, 11)); setForeground(Color.WHITE); setOpaque(false);
            setBorder(new EmptyBorder(3, 9, 3, 9));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(bg); g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g.dispose();
            super.paintComponent(g0);
        }
    }
    public static Badge badge(String t, Color c) { return new Badge(t, c); }

    /** Pill-shaped chip with soft background (used for amenities / specs). */
    public static JLabel chip(String t) {
        JLabel l = new JLabel(t) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(dark ? new Color(51, 65, 85) : new Color(238, 242, 255));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g.dispose();
                super.paintComponent(g0);
            }
        };
        l.setFont(font(Font.PLAIN, 12)); l.setForeground(dark ? TEXT : PRIMARY_DARK);
        l.setOpaque(false); l.setBorder(new EmptyBorder(4, 11, 4, 11));
        return l;
    }
}
