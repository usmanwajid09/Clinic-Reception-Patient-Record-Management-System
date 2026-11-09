package com.clinic.ui;

import com.clinic.config.AppConfig;
import com.clinic.di.ServiceContainer;
import com.clinic.model.DoctorUser;
import com.clinic.model.Role;
import com.clinic.model.User;
import com.clinic.ui.animation.Animator;
import com.clinic.ui.animation.ColorInterpolator;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.panels.*;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Desktop Application Shell Frame.
 * Enforces strict Role-Based Access Control (RBAC) and privacy boundaries:
 * Clinicians, Receptionists, and Administrators only see modules permitted by their authority.
 */
public class MainApplicationFrame extends JFrame {
    private final ServiceContainer container;
    private final CardLayout cardLayout;
    private final JPanel contentCards;
    private final Map<String, NavButton> navButtons = new HashMap<>();

    private String activeCardKey;

    // Component Panels
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

        setTitle(AppConfig.CLINIC_NAME + " - Enterprise Clinic Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(AppConfig.APP_WIDTH, AppConfig.APP_HEIGHT);
        setMinimumSize(new Dimension(1040, 700));

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeColors.BG_LIGHT);

        User currentUser = container.getAuthService().getCurrentUser().orElse(null);
        Role role = currentUser != null ? currentUser.getRole() : Role.RECEPTIONIST;

        // 1. Sidebar Navigation (Filtered strictly by Role)
        JPanel sidebar = createSidebar(currentUser, role);
        root.add(sidebar, BorderLayout.WEST);

        // 2. Top Header Bar
        JPanel headerBar = createHeaderBar(currentUser, role);
        root.add(headerBar, BorderLayout.NORTH);

        // 3. Central Card Container (Instantiate only authorized panels)
        buildAuthorizedPanels(role);
        root.add(contentCards, BorderLayout.CENTER);

        setContentPane(root);

        // Select initial default card based on authenticated role
        selectInitialCardForRole(role);
    }

    private void buildAuthorizedPanels(Role role) {
        switch (role) {
            case DOCTOR -> {
                // Doctor only accesses Clinical Workstation and their own Appointment Queue
                consultationPanel = new DoctorConsultationPanel(container.getConsultationService(),
                        container.getAppointmentService(), container.getPatientService(),
                        container.getDoctorService(), container.getAuthService());
                appointmentPanel = new AppointmentManagementPanel(container.getAppointmentService(),
                        container.getDoctorService(), container.getPatientService(), container.getAuthService());

                contentCards.add(consultationPanel, "CONSULTATIONS");
                contentCards.add(appointmentPanel, "APPOINTMENTS");
            }
            case RECEPTIONIST -> {
                // Receptionist accesses Front-Desk Patient Intake, Doctor Directory, Appointments, and Billing
                patientPanel = new PatientManagementPanel(container.getPatientService(), container.getDoctorService(), container.getAuthService());
                doctorPanel = new DoctorManagementPanel(container.getDoctorService(), container.getAuthService());
                appointmentPanel = new AppointmentManagementPanel(container.getAppointmentService(),
                        container.getDoctorService(), container.getPatientService(), container.getAuthService());
                billingPanel = new BillingManagementPanel(container.getBillingService(), container.getPatientService(), container.getDoctorService());

                contentCards.add(patientPanel, "PATIENTS");
                contentCards.add(doctorPanel, "DOCTORS");
                contentCards.add(appointmentPanel, "APPOINTMENTS");
                contentCards.add(billingPanel, "BILLING");
            }
            case ADMINISTRATOR -> {
                // Administrator accesses Doctor Setup & Schedule, Clinic Analytics Reports, and Audit Views
                doctorPanel = new DoctorManagementPanel(container.getDoctorService(), container.getAuthService());
                reportsPanel = new ReportsPanel(container.getReportService(), container.getPatientService(), container.getDoctorService());
                patientPanel = new PatientManagementPanel(container.getPatientService(), container.getDoctorService(), container.getAuthService());
                appointmentPanel = new AppointmentManagementPanel(container.getAppointmentService(),
                        container.getDoctorService(), container.getPatientService(), container.getAuthService());

                contentCards.add(doctorPanel, "DOCTORS");
                contentCards.add(reportsPanel, "REPORTS");
                contentCards.add(patientPanel, "PATIENTS");
                contentCards.add(appointmentPanel, "APPOINTMENTS");
            }
        }
    }

    private void selectInitialCardForRole(Role role) {
        switch (role) {
            case DOCTOR -> showCard("CONSULTATIONS");
            case RECEPTIONIST -> showCard("PATIENTS");
            case ADMINISTRATOR -> showCard("DOCTORS");
        }
    }

    private JPanel createHeaderBar(User currentUser, Role role) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeColors.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Clinic Branding
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

        // User Profile & Role Switching
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userBox.setOpaque(false);

        String userDisplay = currentUser != null ? currentUser.getFullName() : "Guest";
        userStatusLabel = new JLabel("👤 " + userDisplay);
        userStatusLabel.setFont(UIStyle.FONT_BOLD);
        userStatusLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        ModernButton switchRoleBtn = ModernButton.secondary("Switch Profile / Logout");
        switchRoleBtn.addActionListener(e -> handleSwitchRole());

        userBox.add(userStatusLabel);
        userBox.add(switchRoleBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(userBox, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar(User currentUser, Role role) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ThemeColors.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(22, 14, 20, 14));

        // Brand Banner
        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        JLabel brandName = new JLabel("CLINIC DESKTOP");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandName.setForeground(ThemeColors.PRIMARY_LIGHT);

        JLabel sdaLabel = new JLabel("SDA Healthcare Suite");
        sdaLabel.setFont(UIStyle.FONT_SMALL);
        sdaLabel.setForeground(ThemeColors.TEXT_MUTED);

        // Role Badge Pill
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgePanel.setOpaque(false);
        JLabel roleBadge = new JLabel("  " + role.getDisplayName().toUpperCase() + " SESSION  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        if (role == Role.DOCTOR) {
            roleBadge.setBackground(new Color(13, 148, 136));
            roleBadge.setForeground(Color.WHITE);
        } else if (role == Role.RECEPTIONIST) {
            roleBadge.setBackground(new Color(2, 132, 199));
            roleBadge.setForeground(Color.WHITE);
        } else {
            roleBadge.setBackground(new Color(124, 58, 237));
            roleBadge.setForeground(Color.WHITE);
        }

        badgePanel.add(roleBadge);

        brandBox.add(brandName);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(sdaLabel);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(badgePanel);
        brandBox.add(Box.createVerticalStrut(24));

        sidebar.add(brandBox, BorderLayout.NORTH);

        // Dynamic Role-Filtered Navigation Menu
        JPanel navMenu = new JPanel();
        navMenu.setLayout(new BoxLayout(navMenu, BoxLayout.Y_AXIS));
        navMenu.setOpaque(false);

        switch (role) {
            case DOCTOR -> {
                addNavButton(navMenu, "CONSULTATIONS", "🩺  Clinical Workstation");
                addNavButton(navMenu, "APPOINTMENTS", "📅  My Appointments Queue");
            }
            case RECEPTIONIST -> {
                addNavButton(navMenu, "PATIENTS", "📋  Patient Records");
                addNavButton(navMenu, "DOCTORS", "👨‍⚕️  Doctor Directory & Status");
                addNavButton(navMenu, "APPOINTMENTS", "📅  Appointment Desk");
                addNavButton(navMenu, "BILLING", "💳  Billing & Receipts");
            }
            case ADMINISTRATOR -> {
                addNavButton(navMenu, "DOCTORS", "👨‍⚕️  Doctor Management");
                addNavButton(navMenu, "REPORTS", "📊  Analytics & Revenue");
                addNavButton(navMenu, "PATIENTS", "📋  Patient Directory (Audit)");
                addNavButton(navMenu, "APPOINTMENTS", "📅  All Appointments (Audit)");
            }
        }

        sidebar.add(navMenu, BorderLayout.CENTER);

        // Bottom Footer
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);
        JLabel versionLabel = new JLabel("Role: " + role.getDisplayName());
        versionLabel.setFont(UIStyle.FONT_SMALL);
        versionLabel.setForeground(ThemeColors.TEXT_MUTED);
        bottomBox.add(versionLabel, BorderLayout.SOUTH);
        sidebar.add(bottomBox, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavButton(JPanel containerPanel, String cardKey, String text) {
        NavButton btn = new NavButton(text, cardKey);
        btn.addActionListener(e -> showCard(cardKey));

        navButtons.put(cardKey, btn);
        containerPanel.add(btn);
        containerPanel.add(Box.createVerticalStrut(6));
    }

    public void showCard(String cardKey) {
        this.activeCardKey = cardKey;
        cardLayout.show(contentCards, cardKey);

        // Update active nav styling across buttons
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            boolean isActive = entry.getKey().equals(cardKey);
            entry.getValue().setActive(isActive);
        }

        // Refresh panels on view switch
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

    /**
     * Custom animated sidebar navigation button with smooth hover interpolation.
     */
    private static class NavButton extends JButton {
        private final String cardKey;
        private boolean isActive = false;
        private float hoverProgress = 0.0f;
        private Timer animTimer;

        public NavButton(String text, String cardKey) {
            super(text);
            this.cardKey = cardKey;

            setFont(UIStyle.FONT_BOLD);
            setForeground(ThemeColors.TEXT_ON_DARK);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(11, 16, 11, 16));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            initHoverAnimation();
        }

        private void initHoverAnimation() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isActive) animateHover(1.0f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isActive) animateHover(0.0f);
                }
            });
        }

        private void animateHover(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            final float start = hoverProgress;
            final float delta = target - start;

            animTimer = Animator.animate(130, Animator.Easing.EASE_OUT_QUAD, p -> {
                hoverProgress = start + delta * p;
                repaint();
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            this.hoverProgress = active ? 1.0f : 0.0f;
            setForeground(active ? Color.WHITE : ThemeColors.TEXT_ON_DARK);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isActive) {
                // Active pill background
                g2.setColor(ThemeColors.SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            } else if (hoverProgress > 0.02f) {
                // Smooth hover transition
                Color hoverBg = ColorInterpolator.blend(ThemeColors.SIDEBAR_BG, ThemeColors.SIDEBAR_HOVER, hoverProgress);
                g2.setColor(hoverBg);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
