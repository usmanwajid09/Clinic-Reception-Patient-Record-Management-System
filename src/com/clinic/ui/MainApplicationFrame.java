package com.clinic.ui;

import com.clinic.config.AppConfig;
import com.clinic.di.ServiceContainer;
import com.clinic.model.User;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.panels.*;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Desktop Application Shell Frame.
 * Features modern sidebar navigation, dynamic CardLayout view switching,
 * role-based security context, and status banners.
 */
public class MainApplicationFrame extends JFrame {
    private final ServiceContainer container;
    private final CardLayout cardLayout;
    private final JPanel contentCards;
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Panels
    private PatientManagementPanel patientPanel;
    private DoctorManagementPanel doctorPanel;
    private AppointmentManagementPanel appointmentPanel;
    private DoctorConsultationPanel consultationPanel;
    private BillingManagementPanel billingPanel;
    private ReportsPanel reportsPanel;

    private JLabel userStatusLabel;

    public MainApplicationFrame(ServiceContainer container) {
        this.container = container;
        this.cardLayout = new CardLayout();
        this.contentCards = new JPanel(cardLayout);

        setTitle(AppConfig.CLINIC_NAME + " - Desktop Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(AppConfig.APP_WIDTH, AppConfig.APP_HEIGHT);
        setMinimumSize(new Dimension(1000, 680));

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeColors.BG_LIGHT);

        // 1. Sidebar Navigation
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Top Header Bar
        JPanel headerBar = createHeaderBar();
        root.add(headerBar, BorderLayout.NORTH);

        // 3. Central Card Container
        patientPanel = new PatientManagementPanel(container.getPatientService(), container.getDoctorService());
        doctorPanel = new DoctorManagementPanel(container.getDoctorService());
        appointmentPanel = new AppointmentManagementPanel(container.getAppointmentService(), container.getDoctorService(), container.getPatientService());
        consultationPanel = new DoctorConsultationPanel(container.getConsultationService(), container.getAppointmentService(),
                container.getPatientService(), container.getDoctorService(), container.getAuthService());
        billingPanel = new BillingManagementPanel(container.getBillingService(), container.getPatientService(), container.getDoctorService());
        reportsPanel = new ReportsPanel(container.getReportService(), container.getPatientService(), container.getDoctorService());

        contentCards.add(patientPanel, "PATIENTS");
        contentCards.add(doctorPanel, "DOCTORS");
        contentCards.add(appointmentPanel, "APPOINTMENTS");
        contentCards.add(consultationPanel, "CONSULTATIONS");
        contentCards.add(billingPanel, "BILLING");
        contentCards.add(reportsPanel, "REPORTS");

        root.add(contentCards, BorderLayout.CENTER);

        setContentPane(root);
        showCard("PATIENTS");
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeColors.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Clinic Name & Tagline
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBox.setOpaque(false);
        JLabel clinicTitle = new JLabel("🏥 " + AppConfig.CLINIC_NAME);
        clinicTitle.setFont(UIStyle.FONT_HEADER);
        clinicTitle.setForeground(ThemeColors.PRIMARY_DARK);

        JLabel tagLabel = new JLabel("|  " + AppConfig.CLINIC_TAGLINE);
        tagLabel.setFont(UIStyle.FONT_SMALL);
        tagLabel.setForeground(ThemeColors.TEXT_MUTED);

        titleBox.add(clinicTitle);
        titleBox.add(tagLabel);

        // User Profile & Switch Session
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userBox.setOpaque(false);

        User currentUser = container.getAuthService().getCurrentUser().orElse(null);
        userStatusLabel = new JLabel(currentUser != null ? "👤 " + currentUser.toString() : "Guest");
        userStatusLabel.setFont(UIStyle.FONT_BOLD);
        userStatusLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        ModernButton switchRoleBtn = ModernButton.secondary("Switch Role / Logout");
        switchRoleBtn.addActionListener(e -> handleSwitchRole());

        userBox.add(userStatusLabel);
        userBox.add(switchRoleBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(userBox, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ThemeColors.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        // Nav Brand
        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        JLabel brandName = new JLabel("CLINIC DESKTOP");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brandName.setForeground(ThemeColors.PRIMARY_LIGHT);

        JLabel sdaLabel = new JLabel("SDA Project #2 System");
        sdaLabel.setFont(UIStyle.FONT_SMALL);
        sdaLabel.setForeground(ThemeColors.TEXT_MUTED);

        brandBox.add(brandName);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(sdaLabel);
        brandBox.add(Box.createVerticalStrut(20));

        sidebar.add(brandBox, BorderLayout.NORTH);

        // Navigation Menu Buttons
        JPanel navMenu = new JPanel();
        navMenu.setLayout(new BoxLayout(navMenu, BoxLayout.Y_AXIS));
        navMenu.setOpaque(false);

        addNavButton(navMenu, "PATIENTS", "📋  Patient Records");
        addNavButton(navMenu, "DOCTORS", "👨‍⚕️  Clinicians & Slots");
        addNavButton(navMenu, "APPOINTMENTS", "📅  Appointments Queue");
        addNavButton(navMenu, "CONSULTATIONS", "🩺  Doctor Workstation");
        addNavButton(navMenu, "BILLING", "💳  Billing & Receipts");
        addNavButton(navMenu, "REPORTS", "📊  Analytics & Reports");

        sidebar.add(navMenu, BorderLayout.CENTER);

        // Bottom Info
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);
        JLabel versionLabel = new JLabel("v1.0.0 (Desktop Edition)");
        versionLabel.setFont(UIStyle.FONT_SMALL);
        versionLabel.setForeground(ThemeColors.TEXT_MUTED);
        bottomBox.add(versionLabel, BorderLayout.SOUTH);
        sidebar.add(bottomBox, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavButton(JPanel containerPanel, String cardKey, String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyle.FONT_BOLD);
        btn.setForeground(ThemeColors.TEXT_ON_DARK);
        btn.setBackground(ThemeColors.SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> showCard(cardKey));

        navButtons.put(cardKey, btn);
        containerPanel.add(btn);
        containerPanel.add(Box.createVerticalStrut(4));
    }

    private void showCard(String cardKey) {
        cardLayout.show(contentCards, cardKey);

        // Update active nav styling
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(cardKey)) {
                entry.getValue().setBackground(ThemeColors.SIDEBAR_ACTIVE);
                entry.getValue().setForeground(Color.WHITE);
            } else {
                entry.getValue().setBackground(ThemeColors.SIDEBAR_BG);
                entry.getValue().setForeground(ThemeColors.TEXT_ON_DARK);
            }
        }

        // Refresh dynamic tables when switching tabs
        if ("PATIENTS".equals(cardKey) && patientPanel != null) {
            patientPanel.loadPatients();
        } else if ("DOCTORS".equals(cardKey) && doctorPanel != null) {
            doctorPanel.loadDoctors();
        } else if ("APPOINTMENTS".equals(cardKey) && appointmentPanel != null) {
            appointmentPanel.loadAppointments();
        } else if ("CONSULTATIONS".equals(cardKey) && consultationPanel != null) {
            consultationPanel.loadDoctorQueue();
        } else if ("BILLING".equals(cardKey) && billingPanel != null) {
            billingPanel.loadBills();
        } else if ("REPORTS".equals(cardKey) && reportsPanel != null) {
            reportsPanel.runReports();
        }
    }

    private void handleSwitchRole() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(container);
            loginFrame.setVisible(true);
        });
    }
}
