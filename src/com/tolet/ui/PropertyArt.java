package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.data.Recommender;
import com.tolet.model.Property;
import com.tolet.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/** Draws a unique illustrated "photo" for every listing, so no image files are needed. */
public class PropertyArt extends JComponent {
    private final Property p;
    private final boolean rounded;

    public PropertyArt(Property p, boolean rounded) { this.p = p; this.rounded = rounded; }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        if (rounded) g.setClip(new RoundRectangle2D.Float(0, 0, w, h + 24, 20, 20));
        Random r = new Random(p.id.hashCode() * 31L + p.title.hashCode());

        Color[][] skies = {
                { new Color(255, 183, 94), new Color(237, 117, 134) }, { new Color(110, 197, 233), new Color(190, 233, 255) },
                { new Color(129, 140, 248), new Color(244, 114, 182) }, { new Color(45, 212, 191), new Color(125, 211, 252) },
                { new Color(251, 191, 36), new Color(251, 146, 60) }, { new Color(99, 102, 241), new Color(56, 189, 248) } };
        Color[] sky = skies[Math.abs(p.id.hashCode()) % skies.length];
        g.setPaint(new GradientPaint(0, 0, sky[0], 0, h, sky[1]));
        g.fillRect(0, 0, w, h);

        // sun / moon
        g.setColor(new Color(255, 255, 255, 190));
        int sr = h / 7;
        g.fillOval((int) (w * (0.15 + r.nextDouble() * 0.6)), (int) (h * 0.1), sr, sr);

        // clouds
        g.setColor(new Color(255, 255, 255, 120));
        for (int i = 0; i < 3; i++) {
            int cx = r.nextInt(w), cy = (int) (h * (0.1 + r.nextDouble() * 0.3));
            g.fillRoundRect(cx, cy, 60, 14, 14, 14); g.fillRoundRect(cx + 14, cy - 8, 34, 14, 14, 14);
        }

        // distant skyline
        g.setColor(new Color(0, 0, 0, 40));
        int x = 0;
        while (x < w) { int bw = 18 + r.nextInt(26), bh = 20 + r.nextInt((int) (h * 0.35)); g.fillRect(x, (int) (h * 0.78) - bh, bw, bh); x += bw + 3; }

        // ground
        int gy = (int) (h * 0.78);
        g.setPaint(new GradientPaint(0, gy, new Color(34, 139, 94), 0, h, new Color(21, 94, 66)));
        g.fillRect(0, gy, w, h - gy);

        switch (p.type) {
            case APARTMENT: case COMMERCIAL: tower(g, w, h, gy, r, p.type == Property.Type.COMMERCIAL); break;
            case STUDIO: studio(g, w, h, gy); break;
            default: house(g, w, h, gy, r, p.type == Property.Type.VILLA, p.type == Property.Type.DUPLEX); break;
        }

        // trees
        for (int i = 0; i < 3; i++) {
            int tx = i == 0 ? (int) (w * 0.08) : i == 1 ? (int) (w * 0.9) : (int) (w * (0.05 + r.nextDouble() * 0.9));
            g.setColor(new Color(92, 64, 51)); g.fillRect(tx - 2, gy - 2, 4, 14);
            g.setColor(new Color(22, 120, 70)); g.fillOval(tx - 14, gy - 30, 28, 34);
        }

        // badges
        g.setFont(Ui.font(Font.BOLD, 11));
        badge(g, p.mode.label().toUpperCase(), 12, 12, p.mode == Property.Mode.RENT ? Ui.PRIMARY : Ui.ACCENT);
        if (p.featured) badge(g, "\u2605 FEATURED", w - 92, 12, Ui.WARN);
        User u = Session.user;
        if (u != null && u.role == User.Role.CUSTOMER && h >= 120) {
            badge(g, Recommender.score(u, p) + "% MATCH", 12, h - 30, new Color(17, 24, 39, 200));
        }
        g.dispose();
    }

    private void badge(Graphics2D g, String t, int x, int y, Color c) {
        FontMetrics fm = g.getFontMetrics();
        int bw = fm.stringWidth(t) + 18;
        g.setColor(c); g.fillRoundRect(x, y, bw, 21, 21, 21);
        g.setColor(Color.WHITE); g.drawString(t, x + 9, y + 15);
    }

    private void tower(Graphics2D g, int w, int h, int gy, Random r, boolean office) {
        int bw = (int) (w * 0.34), bh = (int) (h * 0.62), bx = (w - bw) / 2;
        g.setColor(office ? new Color(55, 65, 81) : new Color(241, 245, 249));
        g.fillRect(bx, gy - bh, bw, bh);
        g.setColor(new Color(0, 0, 0, 40)); g.fillRect(bx + bw - 10, gy - bh, 10, bh);
        g.setColor(office ? new Color(75, 85, 99) : new Color(226, 232, 240)); g.fillRect(bx - 6, gy - bh - 8, bw + 12, 8);
        int cols = 4, rows = Math.max(3, bh / 22);
        for (int cI = 0; cI < cols; cI++)
            for (int rI = 0; rI < rows; rI++) {
                boolean lit = r.nextInt(3) != 0;
                g.setColor(lit ? new Color(253, 224, 71) : new Color(125, 160, 200));
                g.fillRoundRect(bx + 10 + cI * ((bw - 20) / cols), gy - bh + 10 + rI * 20, (bw - 20) / cols - 8, 12, 3, 3);
            }
        g.setColor(new Color(120, 53, 15)); g.fillRect(bx + bw / 2 - 9, gy - 20, 18, 20);
    }

    private void studio(Graphics2D g, int w, int h, int gy) {
        int bw = (int) (w * 0.36), bh = (int) (h * 0.32), bx = (w - bw) / 2;
        g.setColor(new Color(254, 243, 199)); g.fillRect(bx, gy - bh, bw, bh);
        g.setColor(new Color(194, 65, 12)); g.fillRect(bx - 8, gy - bh - 8, bw + 16, 10);
        g.setColor(new Color(125, 211, 252)); g.fillRoundRect(bx + 12, gy - bh + 12, bw / 3, bh / 2, 4, 4);
        g.setColor(new Color(120, 53, 15)); g.fillRect(bx + bw - 34, gy - bh / 1.6 > 0 ? (int) (gy - bh / 1.6) : gy - 20, 22, (int) (bh / 1.6));
    }

    private void house(Graphics2D g, int w, int h, int gy, Random r, boolean villa, boolean duplex) {
        int bw = (int) (w * (villa ? 0.5 : 0.4)), floors = duplex || villa ? 2 : 1;
        int fh = (int) (h * 0.22), bh = fh * floors, bx = (w - bw) / 2;
        g.setColor(villa ? new Color(255, 251, 235) : new Color(254, 215, 170)); g.fillRect(bx, gy - bh, bw, bh);
        Path2D roof = new Path2D.Float();
        roof.moveTo(bx - 14, gy - bh); roof.lineTo(bx + bw / 2.0, gy - bh - h * 0.2); roof.lineTo(bx + bw + 14, gy - bh); roof.closePath();
        g.setColor(new Color(185, 28, 28)); g.fill(roof);
        for (int f = 0; f < floors; f++) {
            for (int i = 0; i < 3; i++) {
                if (f == 0 && i == 1) continue;
                g.setColor(r.nextInt(3) == 0 ? new Color(253, 224, 71) : new Color(147, 197, 253));
                g.fillRoundRect(bx + 14 + i * (bw - 28) / 3, gy - bh + 12 + f * fh, (bw - 28) / 3 - 14, fh / 2 - 2, 4, 4);
            }
        }
        g.setColor(new Color(120, 53, 15)); g.fillRoundRect(bx + bw / 2 - 11, gy - fh * 0.62 > 0 ? (int) (gy - fh * 0.62) : gy - 24, 22, (int) (fh * 0.62), 4, 4);
        if (villa) { g.setColor(new Color(56, 189, 248)); g.fillRoundRect(bx + bw + 20, gy + 6, 60, 12, 12, 12); }
    }
}
