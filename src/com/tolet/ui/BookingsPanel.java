package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.*;
import com.tolet.model.User.Role;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Customers see their requests; owners see and answer incoming ones; admins see all. */
public class BookingsPanel extends JPanel implements Refreshable {
    private final DataStore db = DataStore.get();
    private final DefaultTableModel model = Ui.model("ID", "Property", "Type", "Date", "Time", "Offer", "Person", "Status", "Note");
    private final JTable table = new JTable(model);
    private final JLabel sub = Ui.muted("");

    public BookingsPanel() {
        super(new BorderLayout(0, 12)); setOpaque(false);
        Role role = Session.user.role;
        JPanel h = Ui.transparent(new GridLayout(0, 1));
        h.add(Ui.title(role == Role.CUSTOMER ? "My Requests & Visits" : role == Role.OWNER ? "Incoming Requests" : "All Bookings")); h.add(sub);
        add(h, BorderLayout.NORTH);
        Ui.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        add(Ui.scroll(table), BorderLayout.CENTER);

        JPanel bar = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        if (role == Role.CUSTOMER) {
            Ui.RButton cancel = Ui.danger("Cancel Selected");
            cancel.addActionListener(e -> {
                Booking b = selected(); if (b == null) return;
                if (b.status != Booking.Status.PENDING) { Ui.error(this, "Only pending items can be cancelled."); return; }
                b.status = Booking.Status.CANCELLED; db.save(); refresh();
            });
            Ui.RButton doc = Ui.primary("Download Agreement / Receipt");
            doc.addActionListener(e -> {
                Booking b = selected(); if (b == null) return;
                if (b.status != Booking.Status.APPROVED || b.kind == Booking.Kind.VISIT) { Ui.error(this, "Agreements are available for approved rent / buy requests."); return; }
                Documents.save(this, b);
            });
            bar.add(cancel); bar.add(doc);
        } else if (role == Role.OWNER) {
            Ui.RButton ok = Ui.success("Approve"), no = Ui.danger("Decline"), contact = Ui.ghost("Customer Contact");
            ok.addActionListener(e -> decide(true)); no.addActionListener(e -> decide(false));
            contact.addActionListener(e -> {
                Booking b = selected(); if (b == null) return;
                User c = db.user(b.customerId);
                Ui.msg(this, c.name + "\nPhone: " + c.phone + "\nEmail: " + c.email + (b.note.isEmpty() ? "" : "\n\nMessage: " + b.note));
            });
            bar.add(ok); bar.add(no); bar.add(contact);
        }
        add(bar, BorderLayout.SOUTH);
    }

    private Booking selected() {
        int r = table.getSelectedRow();
        if (r < 0) { Ui.error(this, "Please select a row first."); return null; }
        return db.booking((String) model.getValueAt(r, 0));
    }

    private void decide(boolean approve) {
        Booking b = selected(); if (b == null) return;
        if (b.status != Booking.Status.PENDING) { Ui.error(this, "This request was already " + b.status.label().toLowerCase() + "."); return; }
        if (approve && b.kind != Booking.Kind.VISIT && !Ui.confirm(this, "Approving will mark the property as closed and decline other requests. Continue?")) return;
        db.decide(b, approve); refresh();
    }

    @Override public void refresh() {
        User u = Session.user;
        List<Booking> list = u.role == Role.CUSTOMER ? db.bookingsOfCustomer(u.id) : u.role == Role.OWNER ? db.bookingsForOwner(u.id) : db.bookings();
        model.setRowCount(0);
        for (int i = list.size() - 1; i >= 0; i--) {
            Booking b = list.get(i);
            Property p = db.property(b.propertyId); User c = db.user(b.customerId);
            model.addRow(new Object[] { b.id, p == null ? "?" : p.title, b.kind.label(), b.date, b.time.isEmpty() ? "-" : b.time,
                    b.kind == Booking.Kind.VISIT ? "-" : Ui.money(b.offer), u.role == Role.CUSTOMER ? (p == null ? "" : db.user(p.ownerId).name) : c.name,
                    b.status.label(), b.note });
        }
        sub.setText(list.size() + " records");
    }
}
