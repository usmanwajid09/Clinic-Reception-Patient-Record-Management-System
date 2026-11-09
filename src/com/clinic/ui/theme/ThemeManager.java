package com.clinic.ui.theme;

import javax.swing.*;
import java.awt.*;

/**
 * Manages modern desktop Look & Feel setup, font anti-aliasing, and global component defaults.
 */
public final class ThemeManager {
    private ThemeManager() {}

    public static void initialize() {
        // High-DPI font scaling and sub-pixel text rendering
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        boolean flatLafLoaded = false;
        try {
            // Attempt to load FlatLaf modern UI engine
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            java.lang.reflect.Method setupMethod = flatLafClass.getMethod("setup");
            setupMethod.invoke(null);
            flatLafLoaded = true;

            // Global modern component geometries
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Table.rowHeight", 40);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.selectionBackground", new Color(204, 251, 241));
            UIManager.put("Table.selectionForeground", new Color(15, 23, 42));
            UIManager.put("TableHeader.font", UIStyle.FONT_BOLD);
            UIManager.put("TableHeader.separatorColor", new Color(226, 232, 240));

            // Modern font preference
            Font preferredFont = new Font("Segoe UI", Font.PLAIN, 13);
            UIManager.put("defaultFont", preferredFont);
        } catch (Throwable ignored) {
            // FlatLaf not present on classpath; fallback gracefully to native system L&F
        }

        if (!flatLafLoaded) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }
}

