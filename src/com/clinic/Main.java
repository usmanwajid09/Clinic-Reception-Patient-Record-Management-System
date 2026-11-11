package com.clinic;

import com.clinic.ui.RoleSelectFrame;
import com.clinic.ui.theme.ThemeManager;

import javax.swing.*;

/**
 * Desktop GUI Entry Point for Clinic Reception & Patient Record Management System.
 */
public class Main {
    public static void main(String[] args) {
        ThemeManager.initialize();

        SwingUtilities.invokeLater(() -> {
            RoleSelectFrame roleSelectFrame = new RoleSelectFrame();
            roleSelectFrame.setVisible(true);
        });
    }
}
