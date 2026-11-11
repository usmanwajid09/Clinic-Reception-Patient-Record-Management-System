package com.clinic.ui;

import com.clinic.ui.animation.Animator;
import com.clinic.ui.animation.ColorInterpolator;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData.UiRole;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Role Selection Portal Frame (Desktop Swing UI).
 * Presents dedicated role entry cards for Receptionist, Doctor, and Administrator.
 */
public class RoleSelectFrame extends JFrame {

    public RoleSelectFrame() {
        setTitle(UIBranding.CLINIC_NAME + " - Select Role");
        setIconImage(IconFactory.getClinicLogo(64).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 580);
        setMinimumSize(new Dimension(880, 520));
        setResizable(false);

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 28));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(36, 44, 36, 44));

        // Header Section
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel logo = new JLabel(IconFactory.getClinicLogo(64));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel clinicTitle = new JLabel(UIBranding.CLINIC_NAME);
        clinicTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        clinicTitle.setForeground(ThemeColors.PRIMARY_DARK);
        clinicTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel selectPrompt = new JLabel("Select Your Role to Access Desktop Workspace");
        selectPrompt.setFont(UIStyle.FONT_HEADER);
        selectPrompt.setForeground(ThemeColors.TEXT_SECONDARY);
        selectPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(10));
        header.add(clinicTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(selectPrompt);

        root.add(header, BorderLayout.NORTH);

        // 3 Role Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 24, 0));
        cardsGrid.setOpaque(false);

        cardsGrid.add(new RoleCard(
                UiRole.RECEPTIONIST,
                "Receptionist",
                "Manage walk-in patient intake, doctor availability, booking tokens, and cashier billing.",
                IconFactory.getPatientsIcon(32),
                ThemeColors.ACCENT,
                "Continue as Receptionist"
        ));

        cardsGrid.add(new RoleCard(
                UiRole.DOCTOR,
                "Doctor / Clinician",
                "Review assigned daily patient queue, examine history, record diagnoses, and issue Rx.",
                IconFactory.getWorkstationIcon(32),
                ThemeColors.PRIMARY,
                "Continue as Doctor"
        ));

        cardsGrid.add(new RoleCard(
                UiRole.ADMINISTRATOR,
                "Administrator",
                "Configure doctor profiles, consultation slots, and review clinic performance analytics.",
                IconFactory.getReportsIcon(32),
                new Color(124, 58, 237),
                "Continue as Admin"
        ));

        root.add(cardsGrid, BorderLayout.CENTER);

        // Footer Info
        JLabel footerLabel = new JLabel(UIBranding.CLINIC_ADDRESS + "   |   " + UIBranding.CLINIC_CONTACT, SwingConstants.CENTER);
        footerLabel.setFont(UIStyle.FONT_SMALL);
        footerLabel.setForeground(ThemeColors.TEXT_MUTED);
        root.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void openLoginForRole(UiRole role) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(role);
            loginFrame.setVisible(true);
        });
    }

    /**
     * Interactive role selection card with 60 FPS hover border and elevation shadow.
     */
    private class RoleCard extends JPanel {
        private final Color accent;
        private float hoverProgress = 0.0f;
        private Timer animTimer;

        public RoleCard(UiRole role, String title, String description, Icon icon, Color accent, String buttonLabel) {
            this.accent = accent;
            setOpaque(false);
            setLayout(new BorderLayout(0, 16));
            setBorder(new EmptyBorder(26, 22, 24, 22));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Icon pill badge + Title
            JPanel topBox = new JPanel();
            topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
            topBox.setOpaque(false);

            JPanel iconCircle = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 24));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            iconCircle.setOpaque(false);
            iconCircle.setPreferredSize(new Dimension(68, 68));
            iconCircle.setMaximumSize(new Dimension(68, 68));
            iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);
            iconCircle.add(new JLabel(icon));

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLbl.setForeground(ThemeColors.TEXT_PRIMARY);
            titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel descLbl = new JLabel("<html><div style='text-align:center;'>" + description + "</div></html>", SwingConstants.CENTER);
            descLbl.setFont(UIStyle.FONT_REGULAR);
            descLbl.setForeground(ThemeColors.TEXT_SECONDARY);
            descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            topBox.add(iconCircle);
            topBox.add(Box.createVerticalStrut(14));
            topBox.add(titleLbl);
            topBox.add(Box.createVerticalStrut(8));
            topBox.add(descLbl);

            add(topBox, BorderLayout.CENTER);

            ModernButton continueBtn = new ModernButton(buttonLabel, accent, accent.darker(), Color.WHITE);
            continueBtn.setPreferredSize(new Dimension(0, 40));
            continueBtn.addActionListener(e -> openLoginForRole(role));
            add(continueBtn, BorderLayout.SOUTH);

            MouseAdapter hoverHandler = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateHover(1.0f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!RoleCard.this.contains(SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), RoleCard.this))) {
                        animateHover(0.0f);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    openLoginForRole(role);
                }
            };
            addMouseListener(hoverHandler);
        }

        private void animateHover(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            final float start = hoverProgress;
            final float delta = target - start;
            animTimer = Animator.animate(150, Animator.Easing.EASE_OUT_QUAD, p -> {
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

            // Subtle shadow deepening on hover
            int shadowAlpha = 14 + (int) (hoverProgress * 26);
            g2.setColor(new Color(15, 23, 42, shadowAlpha));
            g2.fillRoundRect(3, 5, w - 6, h - 5, 16, 16);

            // Card surface
            g2.setColor(ThemeColors.SURFACE);
            g2.fillRoundRect(1, 1, w - 3, h - 4, 16, 16);

            // Top accent ribbon
            g2.setColor(accent);
            g2.fillRoundRect(1, 1, w - 3, 6, 8, 8);

            // Border interpolation
            g2.setColor(ColorInterpolator.blend(ThemeColors.BORDER, accent, hoverProgress * 0.75f));
            g2.setStroke(new BasicStroke(1.2f + hoverProgress * 0.8f));
            g2.drawRoundRect(1, 1, w - 3, h - 4, 16, 16);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
