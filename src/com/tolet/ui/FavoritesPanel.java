package com.tolet.ui;

import com.tolet.data.DataStore;
import javax.swing.*;
import java.awt.*;

public class FavoritesPanel extends JPanel implements Refreshable {
    private final CardGrid grid = new CardGrid();
    private final JLabel sub = Ui.muted("");
    public FavoritesPanel() {
        super(new BorderLayout(0, 12)); setOpaque(false);
        JPanel h = Ui.transparent(new GridLayout(0, 1)); h.add(Ui.title("My Favorites")); h.add(sub);
        add(h, BorderLayout.NORTH); add(grid, BorderLayout.CENTER);
    }
    @Override public void refresh() {
        var list = DataStore.get().favorites(Session.user.id);
        sub.setText(list.isEmpty() ? "Tap the heart on any listing to save it here." : list.size() + " saved properties");
        grid.setProperties(list, null, this::refresh);
    }
}
