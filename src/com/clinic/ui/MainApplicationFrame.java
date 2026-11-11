package com.clinic.ui;

import com.clinic.ui.animation.Animator;
import com.clinic.ui.animation.ColorInterpolator;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData.UiRole;
import com.clinic.ui.mock.MockClinicData.UiUser;
import com.clinic.ui.panels.*;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Desktop Application Shell Frame (GUI Presentation Layer).
 * Features collapsible role-filtered sidebar, header profile dropdown, and dashboard card navigation.
 */
public class MainApplicationFrame extends JFrame {
    private final UiUser currentUser;
    private final UiRole currentRole;
    private final CardLayout cardLayout;
    private final JPanel contentCards;
    private final Map<String, NavButton> navButtons = new HashMap<>();

    private JPanel sidebar;
    private JPanel brandBox;
    private JLabel versionLabel;
    private boolean sidebarCollapsed = false;

    // UI Panels
    private DashboardHomePanel dashboardPanel;
    private PatientManagementPanel patientPanel;
    private DoctorManagementPanel doctorPanel;
    private AppointmentManagementPanel appointmentPanel;
    private DoctorConsultationPanel consultationPanel;
    private BillingManagementPanel billingPanel;
    private ReportsPanel reportsPanel;

    public MainApplicationFrame(UiUser currentUser) {
        this.currentUser = currentUser;
        this.currentRole = currentUser != null ? currentUser.role() : UiRole.RECEPTIONIST;
        this.cardLayout = new CardLayout();
        this.contentCards = new JPanel(cardLayout);

        setTitle(UIBranding.CLINIC_NAME + " - Desktop Workspace");
        setIconImage(IconFactory.getClinicLogo(64).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UIBranding.APP_WIDTH, UIBranding.APP_HEIGHT);
        setMinimumSize(new Dimension(1040, 700));

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeColors.BG_LIGHT);

        // 1. Collapsible Role-Filtered Sidebar
        sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Top Header Bar
        JPanel headerBar = createHeaderBar();
        root.add(headerBar, BorderLayout.NORTH);

        // 3. Central Card Container
        buildAuthorizedPanels();
        root.add(contentCards, BorderLayout.CENTER);

        setContentPane(root);
        showCard("DASHBOARD");
    }

    private void buildAuthorizedPanels() {
        dashboardPanel = new DashboardHomePanel(currentUser, this::showCard);
        contentCards.add(dashboardPanel, "DASHBOARD");

        switch (currentRole) {
            case DOCTOR -> {
                consultationPanel = new DoctorConsultationPanel(currentUser);
                appointmentPanel = new AppointmentManagementPanel(currentUser);

                contentCards.add(consultationPanel, "CONSULTATIONS");
                contentCards.add(appointmentPanel, "APPOINTMENTS");
            }
            case RECEPTIONIST -> {
                patientPanel = new PatientManagementPanel(currentRole);
                doctorPanel = new DoctorManagementPanel(currentRole);
                appointmentPanel = new AppointmentManagementPanel(currentUser);
                billingPanel = new BillingManagementPanel();

                contentCards.add(patientPanel, "PATIENTS");
                contentCards.add(doctorPanel, "DOCTORS");
                contentCards.add(appointmentPanel, "APPOINTMENTS");
                contentCards.add(billingPanel, "BILLING");
            }
            case ADMINISTRATOR -> {
                doctorPanel = new DoctorManagementPanel(currentRole);
                reportsPanel = new ReportsPanel();
                patientPanel = new PatientManagementPanel(currentRole);
                appointmentPanel = new AppointmentManagementPanel(currentUser);

                contentCards.add(doctorPanel, "DOCTORS");
                contentCards.add(reportsPanel, "REPORTS");
                contentCards.add(patientPanel, "PATIENTS");
                contentCards.add(appointmentPanel, "APPOINTMENTS");
            }
        }
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeColors.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
                new EmptyBorder(10, 20, 10, 24)
        ));

        // Left: Hamburger toggle + Clinic Branding
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBox.setOpaque(false);

        ModernButton collapseBtn = ModernButton.secondary("☰");
        collapseBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        collapseBtn.setPreferredSize(new Dimension(40, 36));
        collapseBtn.setToolTipText("Toggle Sidebar");
        collapseBtn.addActionListener(e -> toggleSidebar());

        JLabel clinicTitle = new JLabel(" " + UIBranding.CLINIC_NAME, IconFactory.getClinicLogo(28), JLabel.LEFT);
        clinicTitle.setFont(UIStyle.FONT_HEADER);
        clinicTitle.setForeground(ThemeColors.PRIMARY_DARK);

        JLabel tagLabel = new JLabel("|  " + UIBranding.CLINIC_TAGLINE);
        tagLabel.setFont(UIStyle.FONT_SMALL);
        tagLabel.setForeground(ThemeColors.TEXT_MUTED);

        titleBox.add(collapseBtn);
        titleBox.add(clinicTitle);
        titleBox.add(tagLabel);

        // Right: User Profile Dropdown + Switch Role Button
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userBox.setOpaque(false);

        String userDisplay = currentUser != null ? currentUser.fullName() : "Guest";
        ImageIcon userIcon = switch (currentRole) {
            case DOCTOR -> IconFactory.getDoctorsIcon(24);
            case RECEPTIONIST -> IconFactory.getPatientsIcon(24);
            case ADMINISTRATOR -> IconFactory.getReportsIcon(24);
        };

        JPopupMenu profileMenu = new JPopupMenu();
        JMenuItem dashItem = new JMenuItem("Dashboard Home");
        dashItem.addActionListener(e -> showCard("DASHBOARD"));
        JMenuItem switchItem = new JMenuItem("Switch Role");
        switchItem.addActionListener(e -> handleSwitchRole());
        JMenuItem logoutItem = new JMenuItem("Sign Out");
        logoutItem.addActionListener(e -> handleLogoutToRoleSelect());
        profileMenu.add(dashItem);
        profileMenu.add(switchItem);
        profileMenu.addSeparator();
        profileMenu.add(logoutItem);

        ModernButton profileDropdownBtn = ModernButton.secondary(userDisplay + " ▾");
        profileDropdownBtn.setIcon(userIcon);
        profileDropdownBtn.setIconTextGap(8);
        profileDropdownBtn.addActionListener(e -> profileMenu.show(profileDropdownBtn, 0, profileDropdownBtn.getHeight()));

        ModernButton switchRoleBtn = new ModernButton("Switch Profile");
        switchRoleBtn.addActionListener(e -> handleLogoutToRoleSelect());

        userBox.add(profileDropdownBtn);
        userBox.add(switchRoleBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(userBox, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(ThemeColors.SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(250, getHeight()));
        sb.setBorder(new EmptyBorder(22, 14, 20, 14));

        // Brand Banner
        brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        JLabel brandName = new JLabel("CLINIC DESKTOP");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandName.setForeground(ThemeColors.PRIMARY_LIGHT);

        JLabel subLabel = new JLabel("Healthcare Suite UI");
        subLabel.setFont(UIStyle.FONT_SMALL);
        subLabel.setForeground(ThemeColors.TEXT_MUTED);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgePanel.setOpaque(false);
        JLabel roleBadge = new JLabel("  " + currentRole.getDisplayName().toUpperCase() + " PORTAL  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        if (currentRole == UiRole.DOCTOR) {
            roleBadge.setBackground(new Color(13, 148, 136));
            roleBadge.setForeground(Color.WHITE);
        } else if (currentRole == UiRole.RECEPTIONIST) {
            roleBadge.setBackground(new Color(2, 132, 199));
            roleBadge.setForeground(Color.WHITE);
        } else {
            roleBadge.setBackground(new Color(124, 58, 237));
            roleBadge.setForeground(Color.WHITE);
        }
        badgePanel.add(roleBadge);

        brandBox.add(brandName);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(subLabel);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(badgePanel);
        brandBox.add(Box.createVerticalStrut(22));

        sb.add(brandBox, BorderLayout.NORTH);

        // Role-Filtered Navigation Menu
        JPanel navMenu = new JPanel();
        navMenu.setLayout(new BoxLayout(navMenu, BoxLayout.Y_AXIS));
        navMenu.setOpaque(false);

        addNavButton(navMenu, "DASHBOARD", "Dashboard", IconFactory.getClinicLogo(24));

        switch (currentRole) {
            case DOCTOR -> {
                addNavButton(navMenu, "CONSULTATIONS", "Clinical Workstation", IconFactory.getWorkstationIcon(24));
                addNavButton(navMenu, "APPOINTMENTS", "My Appointments", IconFactory.getAppointmentsIcon(24));
            }
            case RECEPTIONIST -> {
                addNavButton(navMenu, "PATIENTS", "Patient Records", IconFactory.getPatientsIcon(24));
                addNavButton(navMenu, "DOCTORS", "Doctor Directory", IconFactory.getDoctorsIcon(24));
                addNavButton(navMenu, "APPOINTMENTS", "Appointment Desk", IconFactory.getAppointmentsIcon(24));
                addNavButton(navMenu, "BILLING", "Billing & Receipts", IconFactory.getBillingIcon(24));
            }
            case ADMINISTRATOR -> {
                addNavButton(navMenu, "DOCTORS", "Doctor Management", IconFactory.getDoctorsIcon(24));
                addNavButton(navMenu, "REPORTS", "Analytics & Revenue", IconFactory.getReportsIcon(24));
                addNavButton(navMenu, "PATIENTS", "Patient Directory", IconFactory.getPatientsIcon(24));
                addNavButton(navMenu, "APPOINTMENTS", "Clinic Appointments", IconFactory.getAppointmentsIcon(24));
            }
        }

        sb.add(navMenu, BorderLayout.CENTER);

        // Footer
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);
        versionLabel = new JLabel("Role: " + currentRole.getDisplayName());
        versionLabel.setFont(UIStyle.FONT_SMALL);
        versionLabel.setForeground(ThemeColors.TEXT_MUTED);
        bottomBox.add(versionLabel, BorderLayout.SOUTH);
        sb.add(bottomBox, BorderLayout.SOUTH);

        return sb;
    }

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        sidebar.setPreferredSize(new Dimension(sidebarCollapsed ? 76 : 250, getHeight()));
        brandBox.setVisible(!sidebarCollapsed);
        versionLabel.setVisible(!sidebarCollapsed);
        for (NavButton btn : navButtons.values()) {
            btn.setCompactMode(sidebarCollapsed);
        }
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addNavButton(JPanel containerPanel, String cardKey, String text, Icon icon) {
        NavButton btn = new NavButton(text, icon);
        btn.addActionListener(e -> showCard(cardKey));

        navButtons.put(cardKey, btn);
        containerPanel.add(btn);
        containerPanel.add(Box.createVerticalStrut(6));
    }

    public void showCard(String cardKey) {
        cardLayout.show(contentCards, cardKey);

        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(cardKey));
        }

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
            LoginFrame loginFrame = new LoginFrame(currentRole);
            loginFrame.setVisible(true);
        });
    }

    private void handleLogoutToRoleSelect() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            RoleSelectFrame roleSelect = new RoleSelectFrame();
            roleSelect.setVisible(true);
        });
    }

    /**
     * Custom animated sidebar navigation button with smooth hover interpolation.
     */
    private static class NavButton extends JButton {
        private final String fullText;
        private boolean isActive = false;
        private float hoverProgress = 0.0f;
        private Timer animTimer;

        public NavButton(String text, Icon icon) {
            super(text);
            this.fullText = text;
            setToolTipText(text);

            if (icon != null) {
                setIcon(icon);
                setIconTextGap(12);
            }

            setFont(UIStyle.FONT_BOLD);
            setForeground(ThemeColors.TEXT_ON_DARK);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(11, 14, 11, 14));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        public void setCompactMode(boolean compact) {
            setText(compact ? "" : fullText);
            setHorizontalAlignment(compact ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new EmptyBorder(11, compact ? 0 : 14, 11, compact ? 0 : 14));
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
                g2.setColor(ThemeColors.SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            } else if (hoverProgress > 0.02f) {
                Color hoverBg = ColorInterpolator.blend(ThemeColors.SIDEBAR_BG, ThemeColors.SIDEBAR_HOVER, hoverProgress);
                g2.setColor(hoverBg);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
