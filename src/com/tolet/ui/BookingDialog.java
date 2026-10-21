package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.Booking;
import com.tolet.model.Booking.Kind;
import com.tolet.model.Property;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** Form for scheduling a visit or sending a rent / buy request with an offer. */
public class BookingDialog extends JDialog {
    private final Property p; private final Kind kind;
    private boolean done;
    private final JSpinner date;
    private final JComboBox<String> time = Ui.combo(new String[] { "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM", "06:00 PM" });
    private final JTextField offer = Ui.field(14);
    private final JTextArea note = Ui.area(4, 24);

    public BookingDialog(Window owner, Property p, Kind kind) {
        super(owner, kind.label(), ModalityType.APPLICATION_MODAL);
        this.p = p; this.kind = kind;
        Calendar c = Calendar.getInstance(); c.add(Calendar.DAY_OF_MONTH, 1);
        date = new JSpinner(new SpinnerDateModel(c.getTime(), c.getTime(), null, Calendar.DAY_OF_MONTH));
        date.setEditor(new JSpinner.DateEditor(date, "dd MMM yyyy"));
        date.setFont(Ui.font(Font.PLAIN, 14));
        offer.setText(String.valueOf(Math.round(p.price)));

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Ui.SURFACE); root.setBorder(new EmptyBorder(18, 20, 16, 20));
        root.add(Ui.label(kind.label() + " - " + p.title, Font.BOLD, 17, Ui.TEXT), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Ui.SURFACE);
        int y = 0;
        Ui.row(form, y++, kind == Kind.VISIT ? "Visit date" : "Preferred date", date);
        if (kind == Kind.VISIT) Ui.row(form, y++, "Time", time);
        else Ui.row(form, y++, kind == Kind.RENT_REQUEST ? "Offered rent (BDT/mo)" : "Offered price (BDT)", offer);
        Ui.row(form, y++, "Message", Ui.scroll(note));
        if (kind == Kind.RENT_REQUEST) form.add(Box.createVerticalStrut(1));
        root.add(form, BorderLayout.CENTER);

        JPanel btn = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        Ui.RButton cancel = Ui.ghost("Cancel"); cancel.setForeground(Ui.TEXT);
        cancel.addActionListener(e -> dispose());
        Ui.RButton ok = Ui.primary(kind == Kind.VISIT ? "Book Visit" : "Send Request");
        ok.addActionListener(e -> submit());
        btn.add(cancel); btn.add(ok);
        root.add(btn, BorderLayout.SOUTH);

        setContentPane(root); pack(); setMinimumSize(new Dimension(460, 0)); setLocationRelativeTo(owner);
    }

    private void submit() {
        double off = p.price;
        if (kind != Kind.VISIT) {
            try { off = Double.parseDouble(offer.getText().replace(",", "").trim()); if (off <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { Ui.error(this, "Please enter a valid amount."); return; }
        }
        String d = new SimpleDateFormat("dd MMM yyyy").format((Date) date.getValue());
        DataStore.get().addBooking(p, Session.user, kind, d, kind == Kind.VISIT ? (String) time.getSelectedItem() : "", off, note.getText().trim());
        done = true; dispose();
        Ui.msg(getOwner(), kind == Kind.VISIT ? "Visit booked for " + d + ". The owner has been notified."
                : "Request sent! You'll be notified when the owner responds.");
    }

    public boolean showDialog() { setVisible(true); return done; }
}
