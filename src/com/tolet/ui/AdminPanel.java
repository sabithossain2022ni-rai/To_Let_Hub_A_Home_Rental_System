package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.*;
import com.tolet.model.User.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Admin tools: approvals, user management and CSV reports. */
public class AdminPanel extends JPanel implements Refreshable {
    private final DataStore db = DataStore.get();
    private final DefaultTableModel pend = Ui.model("ID", "Title", "Owner", "Listing", "City", "Price");
    private final DefaultTableModel usr = Ui.model("ID", "Name", "Email", "Phone", "Role", "Joined", "Status");
    private final JTable pt = new JTable(pend), ut = new JTable(usr);
    private final JTextArea report = Ui.area(10, 40);

    public AdminPanel() {
        super(new BorderLayout(0, 12)); setOpaque(false);
        add(Ui.title("Admin Console"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Listing Approvals", approvals());
        tabs.addTab("Users", users());
        tabs.addTab("Reports & Export", reports());
        add(tabs, BorderLayout.CENTER);
    }

    private JComponent approvals() {
        Ui.styleTable(pt);
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(Ui.SURFACE); p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.add(new JScrollPane(pt), BorderLayout.CENTER);
        JPanel bar = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        Ui.RButton ok = Ui.success("Approve"), no = Ui.danger("Reject & Delete"), view = Ui.ghost("Preview");
        ok.addActionListener(e -> { Property x = sel(pt, pend); if (x == null) return; x.status = Property.Status.AVAILABLE;
            db.notify(x.ownerId, "Your listing \"" + x.title + "\" is now live!"); db.save(); refresh(); });
        no.addActionListener(e -> { Property x = sel(pt, pend); if (x == null || !Ui.confirm(this, "Reject and delete this listing?")) return;
            db.notify(x.ownerId, "Your listing \"" + x.title + "\" was rejected."); db.deleteProperty(x.id); refresh(); });
        view.addActionListener(e -> { Property x = sel(pt, pend); if (x != null) new PropertyDialog(SwingUtilities.getWindowAncestor(this), x, null).setVisible(true); });
        bar.add(ok); bar.add(no); bar.add(view); p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private Property sel(JTable t, DefaultTableModel m) {
        int r = t.getSelectedRow();
        if (r < 0) { Ui.error(this, "Please select a row first."); return null; }
        return db.property((String) m.getValueAt(r, 0));
    }

    private JComponent users() {
        Ui.styleTable(ut);
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(Ui.SURFACE); p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.add(new JScrollPane(ut), BorderLayout.CENTER);
        Ui.RButton b = Ui.danger("Block / Unblock");
        b.addActionListener(e -> {
            int r = ut.getSelectedRow(); if (r < 0) { Ui.error(this, "Select a user first."); return; }
            User u = db.user((String) usr.getValueAt(r, 0));
            if (u.role == Role.ADMIN) { Ui.error(this, "Admins cannot be blocked."); return; }
            u.blocked = !u.blocked; db.save(); refresh();
        });
        JPanel bar = Ui.transparent(new FlowLayout(FlowLayout.LEFT)); bar.add(b); p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private JComponent reports() {
        report.setEditable(false); report.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(Ui.SURFACE); p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.add(new JScrollPane(report), BorderLayout.CENTER);
        JPanel bar = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        Ui.RButton a = Ui.primary("Export Listings CSV"), b = Ui.primary("Export Bookings CSV"), c = Ui.ghost("Reset Demo Data");
        a.addActionListener(e -> export("listings.csv", "id,title,mode,type,city,area,price,beds,baths,sqft,status,views\n", () -> {
            StringBuilder s = new StringBuilder();
            for (Property x : db.properties()) s.append(String.join(",", x.id, q(x.title), x.mode.name(), x.type.name(), q(x.city), q(x.area),
                    String.valueOf(Math.round(x.price)), "" + x.bedrooms, "" + x.bathrooms, "" + x.sqft, x.status.name(), "" + x.views)).append("\n");
            return s.toString(); }));
        b.addActionListener(e -> export("bookings.csv", "id,property,customer,kind,date,offer,status\n", () -> {
            StringBuilder s = new StringBuilder();
            for (Booking x : db.bookings()) { Property pp = db.property(x.propertyId); User u = db.user(x.customerId);
                s.append(String.join(",", x.id, q(pp == null ? "?" : pp.title), q(u == null ? "?" : u.name), x.kind.name(), x.date, "" + Math.round(x.offer), x.status.name())).append("\n"); }
            return s.toString(); }));
        c.addActionListener(e -> { if (Ui.confirm(this, "Erase everything and restore the demo data? You will be logged out.")) { db.resetToDemoData(); Session.main.logout(); } });
        bar.add(a); bar.add(b); bar.add(c); p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private static String q(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }

    private void export(String name, String header, java.util.function.Supplier<String> body) {
        JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File(name));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try { Files.write(fc.getSelectedFile().toPath(), (header + body.get()).getBytes(StandardCharsets.UTF_8)); Ui.msg(this, "Exported to " + fc.getSelectedFile().getName()); }
        catch (IOException ex) { Ui.error(this, "Export failed: " + ex.getMessage()); }
    }

    @Override public void refresh() {
        pend.setRowCount(0);
        for (Property p : db.pending()) pend.addRow(new Object[] { p.id, p.title, db.user(p.ownerId).name, p.mode.label(), p.city, Ui.priceOf(p) });
        usr.setRowCount(0);
        for (User u : db.users()) usr.addRow(new Object[] { u.id, u.name, u.email, u.phone, u.role.label(), Ui.day(u.joined), u.blocked ? "Blocked" : "Active" });
        double saleVol = 0, rentVol = 0;
        for (Booking b : db.bookings()) if (b.status == Booking.Status.APPROVED) { if (b.kind == Booking.Kind.BUY_REQUEST) saleVol += b.offer; else if (b.kind == Booking.Kind.RENT_REQUEST) rentVol += b.offer; }
        report.setText(String.format("PLATFORM SUMMARY\n----------------------------------\nUsers            : %d\nListings         : %d (live %d, pending %d)\nBookings         : %d\nReviews          : %d\nApproved sales   : %s\nApproved rent/mo : %s\n",
                db.users().size(), db.properties().size(), db.available().size(), db.pending().size(), db.bookings().size(),
                db.properties().stream().mapToInt(p -> db.reviewsFor(p.id).size()).sum(), Ui.money(saleVol), Ui.money(rentVol)));
    }
}
