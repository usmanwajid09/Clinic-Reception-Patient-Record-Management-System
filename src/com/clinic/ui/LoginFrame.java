package com.clinic.ui;

import com.clinic.config.AppConfig;
import com.clinic.di.ServiceContainer;
import com.clinic.model.User;
import com.clinic.service.IAuthService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Authentication & Role Selector Desktop Frame.
 * Provides pre-configured role profiles for immediate demonstration
 * and conventional username authentication.
 */
public class LoginFrame extends JFrame {
    private final IAuthService authService;
    private final ServiceContainer container;

    private JComboBox<User> profileCombo;

    public LoginFrame(ServiceContainer container) {
        this.container = container;
        this.authService = container.getAuthService();

        setTitle(AppConfig.CLINIC_NAME + " - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(32, 40, 32, 40));

        // Clinic Branding Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("🏥");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(AppConfig.CLINIC_NAME);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Clinic Reception & Patient Record Management System");
        subtitleLabel.setFont(UIStyle.FONT_BOLD);
        subtitleLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel courseLabel = new JLabel("Software Design & Analysis (SDA) Desktop Application");
        courseLabel.setFont(UIStyle.FONT_SMALL);
        courseLabel.setForeground(ThemeColors.TEXT_MUTED);
        courseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoIcon);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(courseLabel);

        root.add(headerPanel, BorderLayout.NORTH);

        // Login Card
        JPanel card = UIStyle.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 180));

        JLabel selectLabel = new JLabel("Select Role Profile for Login:");
        selectLabel.setFont(UIStyle.FONT_BOLD);
        selectLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        selectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<User> demoUsers = authService.getAvailableDemoUsers();
        profileCombo = new JComboBox<>(demoUsers.toArray(new User[0]));
        profileCombo.setFont(UIStyle.FONT_REGULAR);
        profileCombo.setBackground(Color.WHITE);
        profileCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        profileCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tipLabel = new JLabel("<html><i>Switch roles at any time to experience Receptionist, Doctor, or Administrator privileges.</i></html>");
        tipLabel.setFont(UIStyle.FONT_SMALL);
        tipLabel.setForeground(ThemeColors.TEXT_MUTED);
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(selectLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(profileCombo);
        card.add(Box.createVerticalStrut(8));
        card.add(tipLabel);

        root.add(card, BorderLayout.CENTER);

        // Action Button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        ModernButton loginBtn = new ModernButton("Launch Desktop Workspace →");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(380, 44));
        loginBtn.addActionListener(e -> handleLogin());

        buttonPanel.add(loginBtn, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleLogin() {
        User selectedUser = (User) profileCombo.getSelectedItem();
        if (selectedUser != null) {
            authService.setCurrentUser(selectedUser);
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainApplicationFrame frame = new MainApplicationFrame(container);
                frame.setVisible(true);
            });
        }
    }
}
