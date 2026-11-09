package com.clinic;

import com.clinic.di.ServiceContainer;
import com.clinic.ui.LoginFrame;

import javax.swing.*;

/**
 * Desktop Application Entry Point for Clinic Reception & Patient Record Management System.
 * Software Design & Analysis (SDA) Project #2.
 */
public class Main {
    public static void main(String[] args) {
        // Enable high-DPI font scaling and anti-aliasing on Windows
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Set native operating system look and feel for seamless desktop experience
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fallback to default Swing Look & Feel if system L&F fails
            }

            // Initialize dependency injection service container (DIP)
            ServiceContainer container = ServiceContainer.getInstance();

            // Display authentication and role selector frame
            LoginFrame loginFrame = new LoginFrame(container);
            loginFrame.setVisible(true);
        });
    }
}
