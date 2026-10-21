package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.data.Recommender;
import com.tolet.model.*;
import com.tolet.model.User.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/** Role-aware overview with stat cards, charts and recommendations. */
public class DashboardPanel extends JPanel implements Refreshable {
    private final DataStore db = DataStore.get();
    private final JPanel content = new JPanel();

    public DashboardPanel() {
        super(new BorderLayout());
        setOpaque(false);
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JPanel holder = new Ui.VPanel(new BorderLayout()); holder.add(content, BorderLayout.NORTH);
        JScrollPane sp = Ui.scroll(holder); sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); sp.setOpaque(false); sp.getViewport().setOpaque(false);
        add(sp, BorderLayout.CENTER);
    }

    private JComponent stat(String label, String value, Color c) {
        Ui.RPanel p = new Ui.RPanel(new BorderLayout(), 18);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel v = Ui.label(value, Font.BOLD, 30, c);
        p.add(v, BorderLayout.CENTER); p.add(Ui.muted(label), BorderLayout.SOUTH);
        return p;
    }

    @Override public void refresh() {
        content.removeAll();
        User u = Session.user;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greet = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
        JLabel hello = Ui.title(greet + ", " + u.name.split(" ")[0]);
        hello.setAlignmentX(LEFT_ALIGNMENT);
        content.add(hello);
        JLabel sub = Ui.muted("Here is what is happening on ToLet Hub today."); sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub); content.add(Box.createVerticalStrut(14));

        JPanel stats = Ui.transparent(new GridLayout(1, 4, 14, 0));
        stats.setAlignmentX(LEFT_ALIGNMENT); stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        List<Property> live = db.available();
        if (u.role == Role.CUSTOMER) {
            List<Booking> mine = db.bookingsOfCustomer(u.id);
            stats.add(stat("Homes available", String.valueOf(live.size()), Ui.PRIMARY));
            stats.add(stat("Saved favorites", String.valueOf(db.favorites(u.id).size()), Ui.DANGER));
            stats.add(stat("My requests & visits", String.valueOf(mine.size()), Ui.WARN.darker()));
            stats.add(stat("Approved", String.valueOf(mine.stream().filter(b -> b.status == Booking.Status.APPROVED).count()), Ui.ACCENT.darker()));
        } else if (u.role == Role.OWNER) {
            List<Property> own = db.ownedBy(u.id);
            List<Booking> inc = db.bookingsForOwner(u.id);
            stats.add(stat("My listings", String.valueOf(own.size()), Ui.PRIMARY));
            stats.add(stat("Live listings", String.valueOf(own.stream().filter(p -> p.status == Property.Status.AVAILABLE).count()), Ui.ACCENT.darker()));
            stats.add(stat("Pending requests", String.valueOf(inc.stream().filter(b -> b.status == Booking.Status.PENDING).count()), Ui.WARN.darker()));
            stats.add(stat("Total views", String.valueOf(own.stream().mapToInt(p -> p.views).sum()), Ui.DANGER));
        } else {
            stats.add(stat("Registered users", String.valueOf(db.users().size()), Ui.PRIMARY));
            stats.add(stat("Listings", String.valueOf(db.properties().size()), Ui.ACCENT.darker()));
            stats.add(stat("Awaiting approval", String.valueOf(db.pending().size()), Ui.WARN.darker()));
            stats.add(stat("Bookings", String.valueOf(db.bookings().size()), Ui.DANGER));
        }
        content.add(stats); content.add(Box.createVerticalStrut(16));

        // quick actions
        JPanel qa = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0)); qa.setAlignmentX(LEFT_ALIGNMENT);
        Ui.RButton rent = Ui.primary("Browse Rentals"), buy = Ui.success("Browse Homes for Sale"), calc = Ui.ghost("Open Calculators");
        rent.addActionListener(e -> Session.main.browse(1)); buy.addActionListener(e -> Session.main.browse(2)); calc.addActionListener(e -> Session.main.show("calc"));
        qa.add(rent); qa.add(buy); qa.add(calc);
        content.add(qa); content.add(Box.createVerticalStrut(16));

        // charts
        Map<String, Integer> byCity = new TreeMap<>();
        for (Property p : live) byCity.merge(p.city, 1, Integer::sum);
        Map<String, Double> split = new LinkedHashMap<>();
        split.put("For Rent", (double) live.stream().filter(p -> p.mode == Property.Mode.RENT).count());
        split.put("For Sale", (double) live.stream().filter(p -> p.mode == Property.Mode.SALE).count());
        Map<String, Double> types = new LinkedHashMap<>();
        for (Property p : live) types.merge(p.type.label(), 1.0, Double::sum);
        JPanel charts = Ui.transparent(new GridLayout(1, 3, 14, 0));
        charts.setAlignmentX(LEFT_ALIGNMENT); charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        for (JComponent ch : new JComponent[] { new Charts.Bar("Live listings by city", byCity), new Charts.Pie("Rent vs Sale", split), new Charts.Pie("By property type", types) }) {
            Ui.RPanel c = new Ui.RPanel(new BorderLayout(), 18); c.add(ch); charts.add(c);
        }
        content.add(charts); content.add(Box.createVerticalStrut(18));

        // recommendations
        List<Property> picks;
        String heading;
        if (u.role == Role.CUSTOMER) {
            picks = live.stream().sorted(Comparator.comparingInt((Property p) -> Recommender.score(u, p)).reversed()).limit(3).collect(Collectors.toList());
            heading = "Recommended for you";
        } else {
            picks = live.stream().filter(p -> p.featured).limit(3).collect(Collectors.toList());
            heading = "Featured listings";
        }
        JLabel h = Ui.label(heading, Font.BOLD, 18, Ui.TEXT); h.setAlignmentX(LEFT_ALIGNMENT);
        content.add(h); content.add(Box.createVerticalStrut(10));
        content.add(row(picks));

        if (u.role == Role.CUSTOMER) {
            List<Property> rv = db.recentlyViewed(u.id).stream().limit(3).collect(Collectors.toList());
            if (!rv.isEmpty()) {
                content.add(Box.createVerticalStrut(18));
                JLabel h2 = Ui.label("Recently viewed", Font.BOLD, 18, Ui.TEXT); h2.setAlignmentX(LEFT_ALIGNMENT);
                content.add(h2); content.add(Box.createVerticalStrut(10)); content.add(row(rv));
            }
        }
        content.add(Box.createVerticalStrut(20));
        content.revalidate(); content.repaint();
    }

    private JComponent row(List<Property> list) {
        JPanel r = Ui.transparent(new GridLayout(1, 3, 14, 0));
        r.setAlignmentX(LEFT_ALIGNMENT); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        for (Property p : list) r.add(new PropertyCard(p, null, this::refresh));
        for (int i = list.size(); i < 3; i++) r.add(Ui.transparent(null));
        return r;
    }
}
