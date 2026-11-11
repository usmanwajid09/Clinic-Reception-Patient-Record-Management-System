package com.clinic.ui;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.UiRole;
import com.clinic.ui.mock.MockClinicData.UiUser;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Role-Scoped Sign-In Frame (GUI Presentation View).
 * Provides back navigation to role selection and quick demo profile preview.
 */
public class LoginFrame extends JFrame {
    private final UiRole selectedRole;
    private JComboBox<UiUser> profileCombo;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(UiRole selectedRole) {
        this.selectedRole = selectedRole != null ? selectedRole : UiRole.RECEPTIONIST;

        setTitle(UIBranding.CLINIC_NAME + " - " + this.selectedRole.getDisplayName() + " Login");
        setIconImage(IconFactory.getClinicLogo(64).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(24, 38, 30, 38));

        // Top Bar: Back to Role Selection (inspired by Zoro-A login view)
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setOpaque(false);

        ModernButton backBtn = ModernButton.secondary("← Back to Role Selection");
        backBtn.setFont(UIStyle.FONT_SMALL);
        backBtn.addActionListener(e -> handleBack());
        navBar.add(backBtn, BorderLayout.WEST);

        JLabel rolePill = new JLabel("  " + selectedRole.getDisplayName().toUpperCase() + " PORTAL  ");
        rolePill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        rolePill.setOpaque(true);
        rolePill.setForeground(Color.WHITE);
        rolePill.setBorder(new EmptyBorder(5, 10, 5, 10));
        rolePill.setBackground(resolveRoleColor(selectedRole));
        navBar.add(rolePill, BorderLayout.EAST);

        // Branding Center
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel logoIcon = new JLabel(IconFactory.getClinicLogo(64));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(selectedRole.getDisplayName() + " Sign In");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(selectedRole.getDescription());
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(navBar);
        headerPanel.add(Box.createVerticalStrut(14));
        headerPanel.add(logoIcon);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);

        root.add(headerPanel, BorderLayout.NORTH);

        // Login Form Card
        JPanel card = UIStyle.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(410, 235));

        JLabel profileLbl = new JLabel("Active Demo Profile:");
        profileLbl.setFont(UIStyle.FONT_BOLD);
        profileLbl.setForeground(ThemeColors.TEXT_PRIMARY);
        profileLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<UiUser> roleUsers = MockClinicData.getUsersForRole(selectedRole);
        profileCombo = new JComboBox<>(roleUsers.toArray(new UiUser[0]));
        profileCombo.setFont(UIStyle.FONT_REGULAR);
        profileCombo.setBackground(Color.WHITE);
        profileCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        profileCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLbl = new JLabel("Staff Username / Email:");
        userLbl.setFont(UIStyle.FONT_BOLD);
        userLbl.setForeground(ThemeColors.TEXT_PRIMARY);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = UIStyle.createStyledTextField(24);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UIStyle.FONT_BOLD);
        passLbl.setForeground(ThemeColors.TEXT_PRIMARY);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField("clinic@2026");
        passwordField.setFont(UIStyle.FONT_REGULAR);
        passwordField.setBorder(UIStyle.createInputBorder());
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        profileCombo.addActionListener(e -> syncSelectedProfileFields());
        syncSelectedProfileFields();

        card.add(profileLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(profileCombo);
        card.add(Box.createVerticalStrut(12));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);

        root.add(card, BorderLayout.CENTER);

        // Action Button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        ModernButton loginBtn = new ModernButton("Sign In & Launch Workspace →", resolveRoleColor(selectedRole), resolveRoleColor(selectedRole).darker(), Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(410, 44));
        loginBtn.addActionListener(e -> handleLogin());

        buttonPanel.add(loginBtn, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void syncSelectedProfileFields() {
        UiUser selected = (UiUser) profileCombo.getSelectedItem();
        if (selected != null) {
            usernameField.setText(selected.username() + "@shifaclinic.pk");
        }
    }

    private Color resolveRoleColor(UiRole role) {
        return switch (role) {
            case DOCTOR -> ThemeColors.PRIMARY;
            case RECEPTIONIST -> ThemeColors.ACCENT;
            case ADMINISTRATOR -> new Color(124, 58, 237);
        };
    }

    private void handleBack() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            RoleSelectFrame roleSelect = new RoleSelectFrame();
            roleSelect.setVisible(true);
        });
    }

    private void handleLogin() {
        UiUser selectedUser = (UiUser) profileCombo.getSelectedItem();
        if (selectedUser == null && !MockClinicData.getUsersForRole(selectedRole).isEmpty()) {
            selectedUser = MockClinicData.getUsersForRole(selectedRole).get(0);
        }
        final UiUser activeUser = selectedUser;
        dispose();
        SwingUtilities.invokeLater(() -> {
            MainApplicationFrame frame = new MainApplicationFrame(activeUser);
            frame.setVisible(true);
        });
    }
}
