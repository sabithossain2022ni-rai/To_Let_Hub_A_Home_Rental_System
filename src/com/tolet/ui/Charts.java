package com.tolet.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/** Tiny hand-drawn charts (no libraries). */
public final class Charts {
    private Charts() {}

    public static class Bar extends JComponent {
        private final Map<String, Integer> data; private final String title;
        public Bar(String title, Map<String, Integer> data) { this.title = title; this.data = data; setPreferredSize(new Dimension(260, 250)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(Ui.TEXT); g.setFont(Ui.font(Font.BOLD, 14)); g.drawString(title, 16, 26);
            if (data.isEmpty()) { g.setColor(Ui.MUTED); g.drawString("No data", 16, 60); g.dispose(); return; }
            int max = Math.max(1, Collections.max(data.values()));
            int top = 48, bottom = 34, left = 16, n = data.size();
            int slot = (w - left * 2) / n, bw = Math.min(48, slot - 14), i = 0;
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int bh = (int) ((h - top - bottom) * (e.getValue() / (double) max));
                int x = left + i * slot + (slot - bw) / 2, y = h - bottom - bh;
                g.setPaint(new GradientPaint(0, y, Ui.PRIMARY, 0, y + bh, new Color(129, 140, 248)));
                g.fill(new RoundRectangle2D.Float(x, y, bw, bh, 10, 10));
                g.setColor(Ui.TEXT); g.setFont(Ui.font(Font.BOLD, 12));
                String v = String.valueOf(e.getValue());
                g.drawString(v, x + (bw - g.getFontMetrics().stringWidth(v)) / 2, y - 6);
                g.setColor(Ui.MUTED); g.setFont(Ui.font(Font.PLAIN, 11));
                String l = e.getKey().length() > 10 ? e.getKey().substring(0, 9) + "." : e.getKey();
                g.drawString(l, x + (bw - g.getFontMetrics().stringWidth(l)) / 2, h - 14);
                i++;
            }
            g.dispose();
        }
    }

    public static class Pie extends JComponent {
        private final Map<String, Double> data; private final String title;
        private static final Color[] COLORS = { Ui.PRIMARY, Ui.ACCENT, Ui.WARN, Ui.DANGER, new Color(14, 165, 233), new Color(168, 85, 247) };
        public Pie(String title, Map<String, Double> data) { this.title = title; this.data = data; setPreferredSize(new Dimension(260, 250)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Ui.TEXT); g.setFont(Ui.font(Font.BOLD, 14)); g.drawString(title, 16, 26);
            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total <= 0) { g.setColor(Ui.MUTED); g.drawString("No data", 16, 60); g.dispose(); return; }
            int size = Math.min(getHeight() - 70, getWidth() / 2 - 10);
            int x = 20, y = 46;
            double start = 90; int i = 0;
            for (Double v : data.values()) {
                double ext = -360 * v / total;
                g.setColor(COLORS[i % COLORS.length]);
                g.fill(new Arc2D.Double(x, y, size, size, start, ext, Arc2D.PIE));
                start += ext; i++;
            }
            g.setColor(Ui.SURFACE); int hole = size / 2; g.fillOval(x + (size - hole) / 2, y + (size - hole) / 2, hole, hole);
            int ly = y + 10; i = 0;
            g.setFont(Ui.font(Font.PLAIN, 12));
            for (Map.Entry<String, Double> e : data.entrySet()) {
                g.setColor(COLORS[i % COLORS.length]); g.fillRoundRect(x + size + 22, ly - 10, 12, 12, 4, 4);
                g.setColor(Ui.TEXT);
                g.drawString(e.getKey() + "  (" + (e.getValue() < 100 && e.getValue() % 1 != 0 ? String.format("%.0f%%", e.getValue() / total * 100)
                        : String.format("%.0f%%", e.getValue() / total * 100)) + ")", x + size + 40, ly);
                ly += 22; i++;
            }
            g.dispose();
        }
    }
}
