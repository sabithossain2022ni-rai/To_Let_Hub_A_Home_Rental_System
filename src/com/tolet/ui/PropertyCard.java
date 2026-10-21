package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.Property;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

/** A rounded listing card with illustration, specs, favorite heart and compare checkbox. */
public class PropertyCard extends Ui.RPanel {
    public PropertyCard(Property p, Set<String> compare, Runnable onChange) {
        super(new BorderLayout(), 20);
        DataStore db = DataStore.get();
        setPreferredSize(new Dimension(300, 372));

        PropertyArt art = new PropertyArt(p, true);
        art.setPreferredSize(new Dimension(300, 150));
        art.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        art.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { open(p, onChange); }
        });
        add(art, BorderLayout.NORTH);

        JPanel body = Ui.transparent(null);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(12, 16, 6, 16));

        String t = p.title.length() > 30 ? p.title.substring(0, 29) + "\u2026" : p.title;
        JLabel title = Ui.label(t, Font.BOLD, 15, Ui.TEXT);
        JLabel loc = Ui.label(p.location() + "  \u2022  " + p.type.label(), Font.PLAIN, 12, Ui.MUTED);
        JLabel price = Ui.label(Ui.priceOf(p), Font.BOLD, 18, Ui.PRIMARY);
        JLabel specs = Ui.label(spec(p), Font.PLAIN, 12, Ui.TEXT);
        double rating = db.avgRating(p.id);
        int rc = db.reviewsFor(p.id).size();
        JLabel rate = Ui.label(rc == 0 ? "No reviews yet" : Ui.stars(rating) + "  " + String.format("%.1f", rating) + " (" + rc + ")",
                Font.PLAIN, 12, rc == 0 ? Ui.MUTED : Ui.WARN.darker());
        for (JComponent c : new JComponent[] { title, loc, price, specs, rate }) { c.setAlignmentX(LEFT_ALIGNMENT); }
        body.add(title); body.add(Box.createVerticalStrut(3)); body.add(loc); body.add(Box.createVerticalStrut(8));
        body.add(price); body.add(Box.createVerticalStrut(6)); body.add(specs); body.add(Box.createVerticalStrut(4)); body.add(rate);
        add(body, BorderLayout.CENTER);

        JPanel foot = Ui.transparent(new BorderLayout());
        foot.setBorder(new EmptyBorder(0, 12, 12, 12));
        Ui.RButton view = Ui.primary("View Details");
        view.addActionListener(e -> open(p, onChange));
        foot.add(view, BorderLayout.WEST);

        JPanel right = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        if (compare != null) {
            JCheckBox cb = new JCheckBox("Compare", compare.contains(p.id));
            cb.setOpaque(false); cb.setForeground(Ui.MUTED); cb.setFont(Ui.font(Font.PLAIN, 12));
            cb.addActionListener(e -> {
                if (cb.isSelected()) {
                    if (compare.size() >= 3) { cb.setSelected(false); Ui.msg(this, "You can compare up to 3 properties at a time."); return; }
                    compare.add(p.id);
                } else compare.remove(p.id);
                if (onChange != null) onChange.run();
            });
            right.add(cb);
        }
        boolean fav = db.isFav(Session.user.id, p.id);
        JButton heart = new JButton(fav ? "\u2665" : "\u2661");
        heart.setFont(Ui.font(Font.BOLD, 22)); heart.setForeground(fav ? Ui.DANGER : Ui.MUTED);
        heart.setContentAreaFilled(false); heart.setBorderPainted(false); heart.setFocusPainted(false);
        heart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); heart.setToolTipText("Save to favorites");
        heart.addActionListener(e -> {
            boolean now = db.toggleFav(Session.user.id, p.id);
            heart.setText(now ? "\u2665" : "\u2661"); heart.setForeground(now ? Ui.DANGER : Ui.MUTED);
            if (onChange != null) onChange.run();
        });
        right.add(heart);
        foot.add(right, BorderLayout.EAST);
        add(foot, BorderLayout.SOUTH);
    }

    static String spec(Property p) {
        String beds = p.bedrooms == 0 ? "Open plan" : p.bedrooms + " Bed";
        return beds + "  \u2022  " + p.bathrooms + " Bath  \u2022  " + p.sqft + " sqft" + (p.furnished ? "  \u2022  Furnished" : "");
    }

    private void open(Property p, Runnable onChange) {
        new PropertyDialog(SwingUtilities.getWindowAncestor(this), p, onChange).setVisible(true);
    }
}
