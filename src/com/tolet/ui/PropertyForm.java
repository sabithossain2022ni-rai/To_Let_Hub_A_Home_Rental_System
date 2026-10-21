package com.tolet.ui;

import com.tolet.data.DataStore;
import com.tolet.model.Property;
import com.tolet.model.Property.*;
import com.tolet.model.User.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

/** Add / edit listing dialog. */
public class PropertyForm extends JDialog {
    static final String[] AMENITIES = { "WiFi", "Parking", "Lift", "Generator", "Gas", "Security", "Gym", "Swimming Pool", "Balcony", "CCTV", "Rooftop", "Air Conditioning" };
    private final Property existing;
    private boolean saved;
    private final JTextField title = Ui.field(22), area = Ui.field(22), address = Ui.field(22), price = Ui.field(22), sqft = Ui.field(22);
    private final JComboBox<String> city = Ui.combo(new String[] { "Dhaka", "Chattogram", "Sylhet", "Rajshahi", "Khulna", "Gazipur", "Cumilla", "Rangpur" });
    private final JComboBox<Mode> mode = Ui.combo(Mode.values());
    private final JComboBox<Property.Type> type = Ui.combo(Property.Type.values());
    private final JSpinner beds = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1)), baths = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
    private final JCheckBox furnished = new JCheckBox("Furnished");
    private final JTextArea desc = Ui.area(4, 22);
    private final java.util.List<JCheckBox> boxes = new ArrayList<>();

    public PropertyForm(Window owner, Property existing) {
        super(owner, existing == null ? "Add New Listing" : "Edit Listing", ModalityType.APPLICATION_MODAL);
        this.existing = existing;
        city.setEditable(true);
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Ui.SURFACE);
        furnished.setOpaque(false); furnished.setForeground(Ui.TEXT);
        int y = 0;
        Ui.row(form, y++, "Title", title); Ui.row(form, y++, "Listing type", mode); Ui.row(form, y++, "Property type", type);
        Ui.row(form, y++, "City", city); Ui.row(form, y++, "Area / locality", area); Ui.row(form, y++, "Address", address);
        Ui.row(form, y++, "Price (BDT)", price); Ui.row(form, y++, "Bedrooms", beds); Ui.row(form, y++, "Bathrooms", baths);
        Ui.row(form, y++, "Size (sqft)", sqft); Ui.row(form, y++, "", furnished);
        JPanel am = new JPanel(new GridLayout(0, 3)); am.setOpaque(false);
        for (String a : AMENITIES) { JCheckBox c = new JCheckBox(a); c.setOpaque(false); c.setForeground(Ui.TEXT); boxes.add(c); am.add(c); }
        Ui.row(form, y++, "Amenities", am);
        Ui.row(form, y++, "Description", Ui.scroll(desc));

        if (existing != null) {
            title.setText(existing.title); mode.setSelectedItem(existing.mode); type.setSelectedItem(existing.type);
            city.setSelectedItem(existing.city); area.setText(existing.area); address.setText(existing.address);
            price.setText(String.valueOf(Math.round(existing.price))); beds.setValue(existing.bedrooms); baths.setValue(existing.bathrooms);
            sqft.setText(String.valueOf(existing.sqft)); furnished.setSelected(existing.furnished); desc.setText(existing.description);
            for (JCheckBox c : boxes) c.setSelected(existing.amenities.contains(c.getText()));
        }

        JPanel root = new JPanel(new BorderLayout(0, 10)); root.setBackground(Ui.SURFACE); root.setBorder(new EmptyBorder(16, 18, 14, 18));
        root.add(Ui.label(existing == null ? "Add a new listing" : "Edit listing", Font.BOLD, 18, Ui.TEXT), BorderLayout.NORTH);
        root.add(Ui.scroll(form), BorderLayout.CENTER);
        JPanel btn = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        Ui.RButton cancel = Ui.ghost("Cancel"); cancel.setForeground(Ui.TEXT); cancel.addActionListener(e -> dispose());
        Ui.RButton save = Ui.primary("Save Listing"); save.addActionListener(e -> save());
        btn.add(cancel); btn.add(save); root.add(btn, BorderLayout.SOUTH);
        setContentPane(root); setSize(560, 700); setLocationRelativeTo(owner);
    }

    private void save() {
        double pr; int sq;
        try { pr = Double.parseDouble(price.getText().replace(",", "").trim()); sq = Integer.parseInt(sqft.getText().trim()); if (pr <= 0 || sq <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { Ui.error(this, "Price and size must be positive numbers."); return; }
        if (title.getText().trim().isEmpty() || area.getText().trim().isEmpty() || String.valueOf(city.getSelectedItem()).trim().isEmpty()) {
            Ui.error(this, "Title, city and area are required."); return;
        }
        DataStore db = DataStore.get();
        Property p = existing != null ? existing : new Property();
        if (existing == null) { p.id = db.id("P"); p.ownerId = Session.user.id; p.status = Session.user.role == Role.ADMIN ? Status.AVAILABLE : Status.PENDING; }
        p.title = title.getText().trim(); p.mode = (Mode) mode.getSelectedItem(); p.type = (Property.Type) type.getSelectedItem();
        p.city = String.valueOf(city.getSelectedItem()).trim(); p.area = area.getText().trim(); p.address = address.getText().trim();
        p.price = pr; p.sqft = sq; p.bedrooms = (Integer) beds.getValue(); p.bathrooms = (Integer) baths.getValue();
        p.furnished = furnished.isSelected(); p.description = desc.getText().trim();
        p.amenities = new ArrayList<>();
        for (JCheckBox c : boxes) if (c.isSelected()) p.amenities.add(c.getText());
        if (existing == null) { db.addProperty(p); if (p.status == Status.PENDING) for (var u : db.users()) if (u.role == Role.ADMIN) db.notify(u.id, "New listing \"" + p.title + "\" is waiting for approval."); }
        db.save(); saved = true; dispose();
    }

    public boolean showDialog() { setVisible(true); return saved; }
}
