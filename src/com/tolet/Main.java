package com.tolet;

import com.tolet.data.DataStore;
import com.tolet.ui.LoginFrame;

import javax.swing.*;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> {
            DataStore.get();                 // loads (or creates) data/tolet.dat
            new LoginFrame().setVisible(true);
        });
    }
}
