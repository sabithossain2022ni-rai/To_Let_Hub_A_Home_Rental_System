package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.Property;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Owner's "My Listings" management page. */
public class ListingsPanel extends JPanel implements Refreshable {
    private final DataStore db = DataStore.get();
    private final DefaultTableModel model = Ui.model("ID", "Title", "Listing", "Type", "City", "Price", "Status", "Views", "Featured");
    private final JTable table = new JTable(model);
    private final JLabel sub = Ui.muted("");

    public ListingsPanel() {
        super(new BorderLayout(0, 12)); setOpaque(false);
        JPanel h = Ui.transparent(new GridLayout(0, 1)); h.add(Ui.title("My Listings")); h.add(sub);
        add(h, BorderLayout.NORTH);
        Ui.styleTable(table); table.getColumnModel().getColumn(0).setMaxWidth(70);
        add(Ui.scroll(table), BorderLayout.CENTER);

        JPanel bar = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        Ui.RButton add = Ui.success("+ Add Listing"), edit = Ui.primary("Edit"), del = Ui.danger("Delete"),
                toggle = Ui.ghost("Mark Rented/Sold / Reopen"), feat = Ui.ghost("Toggle Featured"), view = Ui.ghost("Preview");
        add.addActionListener(e -> { if (new PropertyForm(SwingUtilities.getWindowAncestor(this), null).showDialog()) {
            refresh(); if (Session.user.role == com.tolet.model.User.Role.OWNER) Ui.msg(this, "Listing submitted! It will go live once an admin approves it."); } });
        edit.addActionListener(e -> { Property p = selected(); if (p != null && new PropertyForm(SwingUtilities.getWindowAncestor(this), p).showDialog()) refresh(); });
        del.addActionListener(e -> { Property p = selected(); if (p != null && Ui.confirm(this, "Delete \"" + p.title + "\" and all its requests?")) { db.deleteProperty(p.id); refresh(); } });
        toggle.addActionListener(e -> {
            Property p = selected(); if (p == null) return;
            if (p.status == Property.Status.PENDING) { Ui.error(this, "This listing is waiting for admin approval."); return; }
            p.status = p.status == Property.Status.CLOSED ? Property.Status.AVAILABLE : Property.Status.CLOSED; db.save(); refresh();
        });
        feat.addActionListener(e -> { Property p = selected(); if (p != null) { p.featured = !p.featured; db.save(); refresh(); } });
        view.addActionListener(e -> { Property p = selected(); if (p != null) { new PropertyDialog(SwingUtilities.getWindowAncestor(this), p, this::refresh).setVisible(true); } });
        for (JComponent c : new JComponent[] { add, edit, del, toggle, feat, view }) bar.add(c);
        add(bar, BorderLayout.SOUTH);
    }

    private Property selected() {
        int r = table.getSelectedRow();
        if (r < 0) { Ui.error(this, "Please select a listing first."); return null; }
        return db.property((String) model.getValueAt(r, 0));
    }

    @Override public void refresh() {
        model.setRowCount(0);
        var list = Session.user.role == com.tolet.model.User.Role.ADMIN ? db.properties() : db.ownedBy(Session.user.id);
        for (Property p : list)
            model.addRow(new Object[] { p.id, p.title, p.mode.label(), p.type.label(), p.city, Ui.priceOf(p), p.status.label(), p.views, p.featured ? "Yes" : "-" });
        long active = list.stream().filter(p -> p.status == Property.Status.AVAILABLE).count();
        sub.setText(list.size() + " listings, " + active + " live. New listings need admin approval.");
    }
}
