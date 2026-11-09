package com.clinic;

import com.clinic.di.ServiceContainer;
import com.clinic.ui.LoginFrame;
import com.clinic.ui.theme.ThemeManager;

import javax.swing.*;

/**
 * Desktop Application Entry Point for Clinic Reception & Patient Record Management System.
 * Software Design & Analysis (SDA) Project #2.
 */
public class Main {
    public static void main(String[] args) {
        ThemeManager.initialize();

        SwingUtilities.invokeLater(() -> {
            ServiceContainer container = ServiceContainer.getInstance();
            LoginFrame loginFrame = new LoginFrame(container);
            loginFrame.setVisible(true);
        });
    }
}
