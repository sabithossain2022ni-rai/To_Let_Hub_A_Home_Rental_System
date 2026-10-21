package com.tolet.ui;

import com.tolet.model.Property;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Set;

/** Scrollable, responsive grid of PropertyCards. */
public class CardGrid extends JPanel {
    private final JPanel grid = new JPanel();
    private final JScrollPane scroll;
    private final JPanel holder = new Ui.VPanel(new BorderLayout());
    private int cols = 3;

    public CardGrid() {
        super(new BorderLayout());
        setOpaque(false);
        grid.setOpaque(false);
        
        holder.add(grid, BorderLayout.NORTH);
        holder.setBorder(BorderFactory.createEmptyBorder(4, 2, 16, 8));
        scroll = Ui.scroll(holder);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int c = Math.max(1, (getWidth() - 20) / 320);
                if (c != cols) { cols = c; relayout(); }
            }
        });
    }

    private void relayout() { grid.setLayout(new GridLayout(0, cols, 18, 18)); grid.revalidate(); grid.repaint(); }

    public void setProperties(List<Property> list, Set<String> compare, Runnable onChange) {
        grid.removeAll();
        if (list.isEmpty()) {
            grid.setLayout(new BorderLayout());
            JLabel l = Ui.label("No properties found. Try changing your filters.", Font.PLAIN, 15, Ui.MUTED);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(BorderFactory.createEmptyBorder(60, 0, 60, 0));
            grid.add(l, BorderLayout.CENTER);
        } else {
            grid.setLayout(new GridLayout(0, cols, 18, 18));
            for (Property p : list) grid.add(new PropertyCard(p, compare, onChange));
        }
        grid.revalidate(); grid.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }
}
