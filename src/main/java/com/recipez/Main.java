package com.recipez;

import com.formdev.flatlaf.FlatDarkLaf;
import com.recipez.core.Application;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(Application::new);
    }
}
