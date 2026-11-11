package com.clinic.ui.panels;

import com.clinic.ui.animation.Animator;
import com.clinic.ui.animation.ColorInterpolator;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.mock.MockClinicData.UiRole;
import com.clinic.ui.mock.MockClinicData.UiUser;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Role-Scoped Dashboard Overview Hub (GUI Presentation Panel).
 * Displays KPI summary cards and clickable quick-launch module tiles tailored to the active role.
 */
public class DashboardHomePanel extends JPanel {
    private final UiUser currentUser;
    private final Consumer<String> navigateAction;

    private StatCard card1;
    private StatCard card2;
    private StatCard card3;

    public DashboardHomePanel(UiUser currentUser, Consumer<String> navigateAction) {
        this.currentUser = currentUser;
        this.navigateAction = navigateAction;

        setLayout(new BorderLayout(0, 22));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        refreshStats();
    }

    private void initComponents() {
        UiRole role = currentUser != null ? currentUser.role() : UiRole.RECEPTIONIST;

        JPanel topSection = new JPanel(new BorderLayout(0, 16));
        topSection.setOpaque(false);

        JPanel greetingPanel = new JPanel(new BorderLayout());
        greetingPanel.setOpaque(false);

        String userName = currentUser != null ? currentUser.fullName() : "Clinic Staff";
        JLabel titleLabel = new JLabel(role.getDisplayName() + " Dashboard — Welcome, " + userName, IconFactory.getClinicLogo(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(UIBranding.CLINIC_NAME + "  |  " + role.getDescription());
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        greetingPanel.add(titleLabel, BorderLayout.NORTH);
        greetingPanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel statsStrip = createRoleStatsStrip(role);

        topSection.add(greetingPanel, BorderLayout.NORTH);
        topSection.add(statsStrip, BorderLayout.CENTER);
        add(topSection, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 12));
        centerWrapper.setOpaque(false);

        JLabel quickTitle = new JLabel("Workspace Modules");
        quickTitle.setFont(UIStyle.FONT_HEADER);
        quickTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        centerWrapper.add(quickTitle, BorderLayout.NORTH);

        JPanel moduleGrid = new JPanel(new GridLayout(0, 2, 18, 18));
        moduleGrid.setOpaque(false);

        switch (role) {
            case RECEPTIONIST -> {
                moduleGrid.add(new ModuleCard("PATIENTS", "Patient Records & Registration",
                        "Register walk-in patients, update demographics, and inspect visit logs.",
                        IconFactory.getPatientsIcon(32), ThemeColors.ACCENT));
                moduleGrid.add(new ModuleCard("DOCTORS", "Doctor Directory & Availability",
                        "Check specialist schedules and toggle daily clinician availability status.",
                        IconFactory.getDoctorsIcon(32), ThemeColors.PRIMARY));
                moduleGrid.add(new ModuleCard("APPOINTMENTS", "Appointment Scheduling Desk",
                        "Issue appointment tokens, reschedule time slots, or manage cancellations.",
                        IconFactory.getAppointmentsIcon(32), new Color(79, 70, 229)));
                moduleGrid.add(new ModuleCard("BILLING", "Cashier Terminal & Receipts",
                        "Settle pending consultation invoices and preview official payment receipts.",
                        IconFactory.getBillingIcon(32), ThemeColors.WARNING));
                moduleGrid.add(new ModuleCard("REPORTS", "Clinic Desk Reports",
                        "Generate appointment lists, patient visit histories, and revenue reports.",
                        IconFactory.getReportsIcon(32), new Color(124, 58, 237)));
            }
            case DOCTOR -> {
                moduleGrid.add(new ModuleCard("CONSULTATIONS", "Clinical Workstation & Rx",
                        "Examine queued patients, enter clinical diagnoses, and build electronic prescriptions.",
                        IconFactory.getWorkstationIcon(32), ThemeColors.PRIMARY));
                moduleGrid.add(new ModuleCard("APPOINTMENTS", "My Scheduled Queue",
                        "Inspect your daily appointment slots, token numbers, and attendance statuses.",
                        IconFactory.getAppointmentsIcon(32), ThemeColors.ACCENT));
                moduleGrid.add(new ModuleCard("DOCTOR_AVAILABILITY", "Mark Duty Availability",
                        "Set your daily availability status (Available or Unavailable) for scheduled dates.",
                        IconFactory.getDoctorsIcon(32), ThemeColors.SUCCESS));
            }
            case ADMINISTRATOR -> {
                moduleGrid.add(new ModuleCard("DOCTORS", "Clinician & Schedule Management",
                        "Onboard physicians, assign departments, configure schedules, and toggle availability.",
                        IconFactory.getDoctorsIcon(32), ThemeColors.PRIMARY));
                moduleGrid.add(new ModuleCard("REPORTS", "Clinical Analytics & Revenue",
                        "Generate appointment, doctor performance, patient history, and revenue reports.",
                        IconFactory.getReportsIcon(32), new Color(124, 58, 237)));
            }
        }

        JPanel gridHolder = new JPanel(new BorderLayout());
        gridHolder.setOpaque(false);
        gridHolder.add(moduleGrid, BorderLayout.NORTH);

        centerWrapper.add(gridHolder, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createRoleStatsStrip(UiRole role) {
        JPanel strip = new JPanel(new GridLayout(1, 3, 14, 0));
        strip.setOpaque(false);

        switch (role) {
            case RECEPTIONIST -> {
                card1 = new StatCard("Registered Patients", "0", "Sample directory profiles", ThemeColors.ACCENT);
                card2 = new StatCard("Scheduled Queue", "0", "Awaiting consultation today", ThemeColors.INFO);
                card3 = new StatCard("Pending Invoices", "0", "Front-desk cashier counter", ThemeColors.WARNING);
            }
            case DOCTOR -> {
                card1 = new StatCard("My Waiting Patients", "0", "Ready in clinical queue", ThemeColors.INFO);
                card2 = new StatCard("Completed Today", "0", "Signed & locked encounters", ThemeColors.SUCCESS);
                card3 = new StatCard("Duty Status", "AVAILABLE", "Current clinician schedule status", ThemeColors.PRIMARY);
            }
            case ADMINISTRATOR -> {
                card1 = new StatCard("Active Clinicians", "0", "Across 5 medical departments", ThemeColors.PRIMARY);
                card2 = new StatCard("Total Appointments", "0", "Logged OPD visits", ThemeColors.INFO);
                card3 = new StatCard("Collected Revenue", UIBranding.CURRENCY_SYMBOL + "0", "Sample billing summary", ThemeColors.SUCCESS);
            }
        }

        strip.add(card1);
        strip.add(card2);
        strip.add(card3);
        return strip;
    }

    public void refreshStats() {
        UiRole role = currentUser != null ? currentUser.role() : UiRole.RECEPTIONIST;
        switch (role) {
            case RECEPTIONIST -> {
                long totalPatients = MockClinicData.getPatients().size();
                long activeAppts = MockClinicData.getAppointments().stream().filter(a -> "SCHEDULED".equals(a.status)).count();
                long unpaidBills = MockClinicData.getBills().stream().filter(b -> "UNPAID".equals(b.status)).count();
                card1.setValue(String.valueOf(totalPatients));
                card2.setValue(String.valueOf(activeAppts));
                card3.setValue(String.valueOf(unpaidBills));
            }
            case DOCTOR -> {
                String docId = currentUser != null && currentUser.linkedDoctorId() != null ? currentUser.linkedDoctorId() : "DOC-101";
                long myPending = MockClinicData.getAppointments().stream()
                        .filter(a -> a.doctorId.equals(docId) && "SCHEDULED".equals(a.status)).count();
                long myDone = MockClinicData.getAppointments().stream()
                        .filter(a -> a.doctorId.equals(docId) && "COMPLETED".equals(a.status)).count();
                DoctorRow doc = MockClinicData.findDoctorById(docId).orElse(null);
                card1.setValue(String.valueOf(myPending));
                card2.setValue(String.valueOf(myDone));
                card3.setValue(doc != null ? doc.status : "AVAILABLE");
            }
            case ADMINISTRATOR -> {
                long totalDoctors = MockClinicData.getDoctors().size();
                double collected = MockClinicData.getBills().stream()
                        .filter(b -> "PAID".equals(b.status)).mapToDouble(b -> b.totalAmount).sum();
                card1.setValue(String.valueOf(totalDoctors));
                card2.setValue(String.valueOf(MockClinicData.getAppointments().size()));
                card3.setValue(UIBranding.CURRENCY_SYMBOL + String.format("%.0f", collected));
            }
        }
    }

    private class ModuleCard extends JPanel {
        private final Color accent;
        private float hoverProgress = 0.0f;
        private Timer animTimer;

        public ModuleCard(String cardKey, String title, String description, Icon icon, Color accent) {
            this.accent = accent;
            setOpaque(false);
            setLayout(new BorderLayout(16, 0));
            setBorder(new EmptyBorder(22, 22, 22, 22));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel iconBox = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            iconBox.setOpaque(false);
            iconBox.setPreferredSize(new Dimension(58, 58));
            iconBox.add(new JLabel(icon));
            add(iconBox, BorderLayout.WEST);

            JPanel textBox = new JPanel();
            textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
            textBox.setOpaque(false);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(UIStyle.FONT_HEADER);
            titleLbl.setForeground(ThemeColors.TEXT_PRIMARY);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel descLbl = new JLabel("<html>" + description + "</html>");
            descLbl.setFont(UIStyle.FONT_REGULAR);
            descLbl.setForeground(ThemeColors.TEXT_SECONDARY);
            descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            textBox.add(titleLbl);
            textBox.add(Box.createVerticalStrut(4));
            textBox.add(descLbl);
            add(textBox, BorderLayout.CENTER);

            JLabel arrowLbl = new JLabel("Open →");
            arrowLbl.setFont(UIStyle.FONT_BOLD);
            arrowLbl.setForeground(accent);
            add(arrowLbl, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateHover(1.0f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animateHover(0.0f);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (navigateAction != null) {
                        navigateAction.accept(cardKey);
                    }
                }
            });
        }

        private void animateHover(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            final float start = hoverProgress;
            final float delta = target - start;
            animTimer = Animator.animate(140, Animator.Easing.EASE_OUT_QUAD, p -> {
                hoverProgress = start + delta * p;
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int shadowAlpha = 12 + (int) (hoverProgress * 22);
            g2.setColor(new Color(15, 23, 42, shadowAlpha));
            g2.fillRoundRect(2, 3, w - 4, h - 4, 14, 14);

            g2.setColor(ThemeColors.SURFACE);
            g2.fillRoundRect(0, 0, w - 2, h - 3, 14, 14);

            g2.setColor(ColorInterpolator.blend(ThemeColors.BORDER, accent, hoverProgress * 0.7f));
            g2.setStroke(new BasicStroke(1.2f + hoverProgress * 0.6f));
            g2.drawRoundRect(0, 0, w - 2, h - 3, 14, 14);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
