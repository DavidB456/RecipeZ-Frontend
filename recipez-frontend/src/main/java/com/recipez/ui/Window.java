package com.recipez.ui;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Window extends JFrame {
    public Window(String TITLE, int WIDTH, int HEIGHT) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        setLocationRelativeTo(null);
        // Optional — only loads if you have /assets/logo.png on the classpath.
        java.net.URL logo = getClass().getResource("/assets/logo.png");
        if (logo != null) {
            setIconImage(new ImageIcon(logo).getImage());
        }
    }

    public void display() {
        setVisible(true);
    }
}
