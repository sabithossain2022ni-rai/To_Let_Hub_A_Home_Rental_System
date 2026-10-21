package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.*;
import com.tolet.model.Property.Mode;
import com.tolet.model.User.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/** Full details of one property: overview, reviews, finance helper and actions. */
public class PropertyDialog extends JDialog {
    private final Property p;
    private final DataStore db = DataStore.get();
    private final Runnable onChange;

    public PropertyDialog(Window owner, Property p, Runnable onChange) {
        super(owner, p.title, ModalityType.APPLICATION_MODAL);
        this.p = p; this.onChange = onChange;
        p.views++;
        db.addViewed(Session.user.id, p.id);
        db.save();

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Ui.SURFACE);
        PropertyArt art = new PropertyArt(p, false);
        art.setPreferredSize(new Dimension(820, 210));
        root.add(art, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", overview());
        tabs.addTab("Reviews (" + db.reviewsFor(p.id).size() + ")", reviews());
        tabs.addTab(p.mode == Mode.SALE ? "Loan / EMI" : "Move-in Cost", finance());
        tabs.setBorder(new EmptyBorder(6, 10, 0, 10));
        root.add(tabs, BorderLayout.CENTER);
        root.add(actions(), BorderLayout.SOUTH);

        setContentPane(root);
        setSize(860, 740);
        setLocationRelativeTo(owner);
    }

    private JComponent overview() {
        JPanel pn = new JPanel();
        pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
        pn.setBackground(Ui.SURFACE); pn.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel head = Ui.transparent(new BorderLayout());
        JPanel left = Ui.transparent(new GridLayout(0, 1, 0, 3));
        left.add(Ui.label(p.title, Font.BOLD, 22, Ui.TEXT));
        left.add(Ui.label(p.address.isEmpty() ? p.location() : p.address + ", " + p.location(), Font.PLAIN, 13, Ui.MUTED));
        head.add(left, BorderLayout.CENTER);
        head.add(Ui.label(Ui.priceOf(p), Font.BOLD, 24, Ui.PRIMARY), BorderLayout.EAST);
        head.setAlignmentX(LEFT_ALIGNMENT);
        pn.add(head); pn.add(Box.createVerticalStrut(12));

        JPanel chips = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.add(Ui.chip(p.type.label()));
        chips.add(Ui.chip(p.bedrooms == 0 ? "Open plan" : p.bedrooms + " Bedrooms"));
        chips.add(Ui.chip(p.bathrooms + " Bathrooms"));
        chips.add(Ui.chip(p.sqft + " sqft"));
        chips.add(Ui.chip(p.furnished ? "Furnished" : "Unfurnished"));
        chips.add(Ui.chip(p.views + " views"));
        chips.add(Ui.chip(String.format("BDT %,d / sqft", Math.round(p.price / Math.max(1, p.sqft)))
                .replace("/ sqft", p.mode == Mode.RENT ? "/ sqft / mo" : "/ sqft")));
        chips.setAlignmentX(LEFT_ALIGNMENT);
        pn.add(chips); pn.add(Box.createVerticalStrut(14));

        JLabel dh = Ui.label("About this property", Font.BOLD, 15, Ui.TEXT); dh.setAlignmentX(LEFT_ALIGNMENT);
        pn.add(dh); pn.add(Box.createVerticalStrut(4));
        JTextArea desc = new JTextArea(p.description);
        desc.setEditable(false); desc.setLineWrap(true); desc.setWrapStyleWord(true); desc.setOpaque(false);
        desc.setForeground(Ui.TEXT); desc.setFont(Ui.font(Font.PLAIN, 13)); desc.setAlignmentX(LEFT_ALIGNMENT);
        desc.setBorder(null);
        pn.add(desc); pn.add(Box.createVerticalStrut(14));

        JLabel ah = Ui.label("Amenities", Font.BOLD, 15, Ui.TEXT); ah.setAlignmentX(LEFT_ALIGNMENT);
        pn.add(ah); pn.add(Box.createVerticalStrut(6));
        JPanel am = Ui.transparent(new GridLayout(0, 4, 6, 6));
        for (String a : p.amenities) am.add(Ui.chip("\u2713 " + a));
        if (p.amenities.isEmpty()) am.add(Ui.muted("None listed"));
        am.setAlignmentX(LEFT_ALIGNMENT);
        pn.add(am); pn.add(Box.createVerticalStrut(14));

        User o = db.user(p.ownerId);
        Ui.RPanel card = new Ui.RPanel(new BorderLayout(), 14);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JPanel info = Ui.transparent(new GridLayout(0, 1));
        info.add(Ui.label("Listed by " + (o == null ? "Unknown" : o.name), Font.BOLD, 14, Ui.TEXT));
        info.add(Ui.muted(o == null ? "" : "Phone: " + o.phone + "   |   Email: " + o.email));
        card.add(info, BorderLayout.CENTER);
        pn.add(card);

        JScrollPane sp = Ui.scroll(wrap(pn)); sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); return sp;
    }

    private JComponent wrap(JComponent c) {
        JPanel w = new Ui.VPanel(new BorderLayout()); w.setOpaque(true); w.setBackground(Ui.SURFACE); w.add(c, BorderLayout.NORTH); return w;
    }

    private JComponent reviews() {
        JPanel pn = new JPanel(new BorderLayout(0, 10));
        pn.setBackground(Ui.SURFACE); pn.setBorder(new EmptyBorder(14, 14, 14, 14));
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS)); list.setBackground(Ui.SURFACE);
        List<Review> rs = db.reviewsFor(p.id);
        if (rs.isEmpty()) list.add(Ui.muted("No reviews yet. Be the first to share your experience!"));
        for (Review r : rs) {
            User u = db.user(r.userId);
            Ui.RPanel c = new Ui.RPanel(new BorderLayout(), 12);
            c.setBorder(new EmptyBorder(10, 14, 10, 14));
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            c.setAlignmentX(LEFT_ALIGNMENT);
            JPanel t = Ui.transparent(new BorderLayout());
            t.add(Ui.label(u == null ? "User" : u.name, Font.BOLD, 13, Ui.TEXT), BorderLayout.WEST);
            t.add(Ui.label(Ui.stars(r.rating) + "  " + Ui.day(r.created), Font.PLAIN, 12, Ui.WARN.darker()), BorderLayout.EAST);
            c.add(t, BorderLayout.NORTH);
            c.add(Ui.label(r.comment, Font.PLAIN, 13, Ui.TEXT), BorderLayout.CENTER);
            list.add(c); list.add(Box.createVerticalStrut(8));
        }
        pn.add(Ui.scroll(wrap(list)), BorderLayout.CENTER);

        if (Session.user.role != Role.ADMIN && !Session.user.id.equals(p.ownerId)) {
            JPanel add = Ui.transparent(new BorderLayout(8, 0));
            JComboBox<Integer> rating = Ui.combo(new Integer[] { 5, 4, 3, 2, 1 });
            JTextField comment = Ui.field(24);
            Ui.RButton post = Ui.primary("Post Review");
            post.addActionListener(e -> {
                if (comment.getText().trim().isEmpty()) { Ui.error(this, "Please write a short comment."); return; }
                db.addReview(p.id, Session.user, (Integer) rating.getSelectedItem(), comment.getText().trim());
                dispose();
                if (onChange != null) onChange.run();
                new PropertyDialog(getOwner(), p, onChange).setVisible(true);
            });
            JPanel l = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
            l.add(Ui.label("Rating", Font.BOLD, 13, Ui.TEXT)); l.add(rating);
            add.add(l, BorderLayout.WEST); add.add(comment, BorderLayout.CENTER); add.add(post, BorderLayout.EAST);
            pn.add(add, BorderLayout.SOUTH);
        }
        return pn;
    }

    private JComponent finance() {
        JPanel pn = new JPanel(new GridBagLayout());
        pn.setBackground(Ui.SURFACE); pn.setBorder(new EmptyBorder(16, 16, 16, 16));
        if (p.mode == Mode.RENT) {
            double dep = p.price * 2, service = Math.round(p.price * 0.05);
            String[][] rows = {
                    { "Monthly rent", Ui.money(p.price) }, { "Security deposit (2 months)", Ui.money(dep) },
                    { "Estimated service charge / month", Ui.money(service) },
                    { "Total move-in cost (deposit + 1st month)", Ui.money(dep + p.price) },
                    { "Yearly cost (rent + service)", Ui.money((p.price + service) * 12) } };
            int y = 0;
            for (String[] r : rows) Ui.row(pn, y++, r[0], Ui.label(r[1], Font.BOLD, 14, Ui.PRIMARY));
            GridBagConstraints g = new GridBagConstraints(); g.gridy = y; g.gridwidth = 2; g.weighty = 1; g.anchor = GridBagConstraints.NORTHWEST;
            g.insets = new Insets(14, 4, 0, 0);
            pn.add(Ui.muted("Estimates only. Final terms are decided by the owner."), g);
        } else {
            JSlider down = new JSlider(10, 90, 30), rate = new JSlider(40, 160, 95), years = new JSlider(1, 30, 20);
            for (JSlider s : new JSlider[] { down, rate, years }) s.setBackground(Ui.SURFACE);
            JLabel dl = Ui.label("", Font.BOLD, 13, Ui.TEXT), rl = Ui.label("", Font.BOLD, 13, Ui.TEXT), yl = Ui.label("", Font.BOLD, 13, Ui.TEXT);
            JLabel emi = Ui.label("", Font.BOLD, 26, Ui.PRIMARY), det = Ui.label("", Font.PLAIN, 13, Ui.MUTED);
            Runnable calc = () -> {
                double r = rate.getValue() / 10.0;
                dl.setText("Down payment: " + down.getValue() + "%  (" + Ui.money(p.price * down.getValue() / 100) + ")");
                rl.setText("Interest rate: " + r + "% p.a."); yl.setText("Loan term: " + years.getValue() + " years");
                double[] res = CalculatorPanel.emi(p.price * (100 - down.getValue()) / 100, r, years.getValue());
                emi.setText(Ui.money(res[0]) + " / month");
                det.setText("Total interest " + Ui.money(res[2]) + "   |   Total payable " + Ui.money(res[1]));
            };
            javax.swing.event.ChangeListener cl = e -> calc.run();
            down.addChangeListener(cl); rate.addChangeListener(cl); years.addChangeListener(cl);
            Ui.row(pn, 0, "", dl); Ui.row(pn, 1, "", down); Ui.row(pn, 2, "", rl); Ui.row(pn, 3, "", rate);
            Ui.row(pn, 4, "", yl); Ui.row(pn, 5, "", years); Ui.row(pn, 6, "Estimated EMI", emi); Ui.row(pn, 7, "", det);
            GridBagConstraints g = new GridBagConstraints(); g.gridy = 8; g.weighty = 1; pn.add(Box.createGlue(), g);
            calc.run();
        }
        return pn;
    }

    private JComponent actions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bar.setBackground(Ui.SURFACE); bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.BORDER));
        User u = Session.user;
        boolean mine = u.id.equals(p.ownerId);
        boolean canBook = u.role == Role.CUSTOMER && p.status == Property.Status.AVAILABLE;

        Ui.RButton close = Ui.ghost("Close");
        close.setForeground(Ui.TEXT);
        close.addActionListener(e -> { dispose(); if (onChange != null) onChange.run(); });
        bar.add(close);
        if (mine) bar.add(Ui.muted("This is your listing"));
        else if (u.role == Role.CUSTOMER) {
            if (p.status != Property.Status.AVAILABLE) bar.add(Ui.label("No longer available", Font.BOLD, 13, Ui.DANGER));
            Ui.RButton visit = Ui.ghost("Schedule Visit");
            visit.setEnabled(canBook);
            visit.setForeground(Ui.TEXT);
            visit.addActionListener(e -> { if (new BookingDialog(this, p, Booking.Kind.VISIT).showDialog()) dispose(); });
            bar.add(visit);
            Ui.RButton go = p.mode == Mode.RENT ? Ui.primary("Rent This Home") : Ui.success("Buy This Home");
            go.setEnabled(canBook);
            go.addActionListener(e -> {
                if (new BookingDialog(this, p, p.mode == Mode.RENT ? Booking.Kind.RENT_REQUEST : Booking.Kind.BUY_REQUEST).showDialog()) {
                    dispose(); if (onChange != null) onChange.run();
                }
            });
            bar.add(go);
        }
        return bar;
    }
}
