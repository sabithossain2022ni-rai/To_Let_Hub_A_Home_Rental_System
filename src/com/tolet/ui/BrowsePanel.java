package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.data.Recommender;
import com.tolet.model.Property;
import com.tolet.model.Property.Mode;
import com.tolet.model.Property.Type;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/** Search + filter + sort + compare page for all available listings. */
public class BrowsePanel extends JPanel implements Refreshable {
    private final DataStore db = DataStore.get();
    private final JTextField search = Ui.field(16), minP = Ui.field(6), maxP = Ui.field(6);
    private final JComboBox<String> mode = Ui.combo(new String[] { "Rent & Sale", "For Rent", "For Sale" });
    private final JComboBox<String> type = Ui.combo(new String[] { "All Types", "Apartment", "House", "Villa", "Studio", "Duplex", "Commercial" });
    private final JComboBox<String> city = Ui.combo(new String[] { "All Cities" });
    private final JComboBox<String> beds = Ui.combo(new String[] { "Any Beds", "1+", "2+", "3+", "4+" });
    private final JComboBox<String> sort = Ui.combo(new String[] { "Best Match", "Newest", "Price: Low to High", "Price: High to Low", "Largest Area", "Most Viewed", "Top Rated" });
    private final JCheckBox furnished = new JCheckBox("Furnished");
    private final JLabel count = Ui.muted("");
    private final CardGrid grid = new CardGrid();
    private final Set<String> compare = new LinkedHashSet<>();
    private final Ui.RButton compareBtn = Ui.primary("Compare (0)");
    private boolean loading;

    public BrowsePanel() {
        super(new BorderLayout(0, 12));
        setOpaque(false);

        Ui.RPanel filters = new Ui.RPanel(new GridBagLayout(), 18);
        filters.setBorder(new EmptyBorder(12, 14, 12, 14));
        furnished.setOpaque(false); furnished.setForeground(Ui.TEXT);
        search.setToolTipText("Search by title, area, city or amenity");
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.fill = GridBagConstraints.HORIZONTAL; g.gridy = 0;
        g.gridx = 0; g.gridwidth = 2; g.weightx = 1; filters.add(search, g);
        g.gridwidth = 1; g.weightx = 0;
        g.gridx = 2; filters.add(mode, g); g.gridx = 3; filters.add(type, g); g.gridx = 4; filters.add(city, g);
        g.gridx = 5; filters.add(beds, g);
        g.gridy = 1;
        g.gridx = 0; filters.add(Ui.muted("Price (BDT):"), g);
        g.gridx = 1; JPanel pr = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 4, 0)); pr.add(minP); pr.add(Ui.muted("to")); pr.add(maxP); filters.add(pr, g);
        g.gridx = 2; filters.add(furnished, g);
        g.gridx = 3; filters.add(sort, g);
        g.gridx = 4; Ui.RButton apply = Ui.primary("Search"); filters.add(apply, g);
        g.gridx = 5; Ui.RButton reset = Ui.ghost("Reset"); filters.add(reset, g);

        apply.addActionListener(e -> refresh());
        reset.addActionListener(e -> reset());
        search.addActionListener(e -> refresh());
        minP.addActionListener(e -> refresh()); maxP.addActionListener(e -> refresh());
        for (JComboBox<?> c : Arrays.asList(mode, type, city, beds, sort)) c.addActionListener(e -> { if (!loading) refresh(); });
        furnished.addActionListener(e -> refresh());

        JPanel top = Ui.transparent(new BorderLayout(0, 10));
        JPanel titleRow = Ui.transparent(new BorderLayout());
        JPanel tl = Ui.transparent(new GridLayout(0, 1));
        tl.add(Ui.title("Find your perfect home")); tl.add(count);
        titleRow.add(tl, BorderLayout.WEST);
        compareBtn.addActionListener(e -> showCompare());
        titleRow.add(compareBtn, BorderLayout.EAST);
        top.add(titleRow, BorderLayout.NORTH); top.add(filters, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    private void reset() {
        loading = true;
        search.setText(""); minP.setText(""); maxP.setText(""); furnished.setSelected(false);
        mode.setSelectedIndex(0); type.setSelectedIndex(0); city.setSelectedIndex(0); beds.setSelectedIndex(0); sort.setSelectedIndex(0);
        loading = false;
        refresh();
    }

    /** Lets other pages jump here with a preset mode (0 all, 1 rent, 2 sale). */
    public void presetMode(int m) { loading = true; mode.setSelectedIndex(m); loading = false; refresh(); }

    @Override public void refresh() {
        loading = true;
        Object sel = city.getSelectedItem();
        city.removeAllItems(); city.addItem("All Cities");
        for (String c : db.cities()) city.addItem(c);
        city.setSelectedItem(sel == null ? "All Cities" : sel);
        loading = false;

        double min = parse(minP.getText(), 0), max = parse(maxP.getText(), Double.MAX_VALUE);
        String q = search.getText().trim().toLowerCase();
        int minBeds = beds.getSelectedIndex();
        List<Property> list = db.available().stream().filter(p -> {
            if (mode.getSelectedIndex() == 1 && p.mode != Mode.RENT) return false;
            if (mode.getSelectedIndex() == 2 && p.mode != Mode.SALE) return false;
            if (type.getSelectedIndex() > 0 && p.type != Type.values()[type.getSelectedIndex() - 1]) return false;
            if (city.getSelectedIndex() > 0 && !p.city.equals(city.getSelectedItem())) return false;
            if (p.bedrooms < minBeds) return false;
            if (furnished.isSelected() && !p.furnished) return false;
            if (p.price < min || p.price > max) return false;
            if (!q.isEmpty()) {
                String hay = (p.title + " " + p.area + " " + p.city + " " + p.address + " " + p.type + " " + String.join(" ", p.amenities)).toLowerCase();
                for (String w : q.split("\\s+")) if (!hay.contains(w)) return false;
            }
            return true;
        }).collect(Collectors.toList());

        Comparator<Property> cmp;
        switch (sort.getSelectedIndex()) {
            case 1: cmp = Comparator.comparingLong((Property p) -> p.created).reversed(); break;
            case 2: cmp = Comparator.comparingDouble(p -> p.price); break;
            case 3: cmp = Comparator.comparingDouble((Property p) -> p.price).reversed(); break;
            case 4: cmp = Comparator.comparingInt((Property p) -> p.sqft).reversed(); break;
            case 5: cmp = Comparator.comparingInt((Property p) -> p.views).reversed(); break;
            case 6: cmp = Comparator.comparingDouble((Property p) -> db.avgRating(p.id)).reversed(); break;
            default: cmp = Comparator.comparingInt((Property p) -> Recommender.score(Session.user, p)).reversed();
        }
        list.sort(cmp);
        compare.removeIf(id -> db.property(id) == null);
        count.setText(list.size() + " properties found");
        updateCompare();
        grid.setProperties(list, compare, this::updateCompare);
    }

    private void updateCompare() { compareBtn.setText("Compare (" + compare.size() + ")"); compareBtn.setEnabled(compare.size() >= 2); }

    private static double parse(String s, double def) {
        try { return s.trim().isEmpty() ? def : Double.parseDouble(s.replace(",", "").trim()); } catch (NumberFormatException e) { return def; }
    }

    private void showCompare() {
        List<Property> ps = compare.stream().map(db::property).filter(Objects::nonNull).collect(Collectors.toList());
        String[] cols = new String[ps.size() + 1]; cols[0] = "Feature";
        for (int i = 0; i < ps.size(); i++) cols[i + 1] = ps.get(i).title;
        javax.swing.table.DefaultTableModel m = Ui.model(cols);
        String[] names = { "Price", "Listing", "Type", "Location", "Bedrooms", "Bathrooms", "Area (sqft)", "Price / sqft", "Furnished", "Amenities", "Rating", "Views" };
        for (int r = 0; r < names.length; r++) {
            Object[] row = new Object[cols.length]; row[0] = names[r];
            for (int i = 0; i < ps.size(); i++) {
                Property p = ps.get(i); String v;
                switch (r) {
                    case 0: v = Ui.priceOf(p); break; case 1: v = p.mode.label(); break; case 2: v = p.type.label(); break;
                    case 3: v = p.location(); break; case 4: v = String.valueOf(p.bedrooms); break; case 5: v = String.valueOf(p.bathrooms); break;
                    case 6: v = String.valueOf(p.sqft); break; case 7: v = Ui.money(p.price / Math.max(1, p.sqft)); break;
                    case 8: v = p.furnished ? "Yes" : "No"; break; case 9: v = p.amenities.size() + " (" + String.join(", ", p.amenities.subList(0, Math.min(3, p.amenities.size()))) + "...)"; break;
                    case 10: v = db.reviewsFor(p.id).isEmpty() ? "-" : String.format("%.1f / 5", db.avgRating(p.id)); break;
                    default: v = String.valueOf(p.views);
                }
                row[i + 1] = v;
            }
            m.addRow(row);
        }
        JTable t = new JTable(m); Ui.styleTable(t); t.setRowHeight(36);
        t.getColumnModel().getColumn(0).setPreferredWidth(110);
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Compare Properties", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 10)); root.setBackground(Ui.SURFACE); root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.add(Ui.label("Side-by-side comparison", Font.BOLD, 18, Ui.TEXT), BorderLayout.NORTH);
        root.add(new JScrollPane(t), BorderLayout.CENTER);
        d.setContentPane(root); d.setSize(900, 520); d.setLocationRelativeTo(this); d.setVisible(true);
    }
}
