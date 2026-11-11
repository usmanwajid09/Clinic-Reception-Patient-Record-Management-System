package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Modal UI dialog for toggling doctor availability status visually.
 */
public class DoctorAvailabilityDialog extends JDialog {
    private final DoctorRow doctor;
    private boolean updated = false;

    private JTextField dateField;
    private JRadioButton availableRadio;
    private JRadioButton unavailableRadio;

    public DoctorAvailabilityDialog(Window owner, DoctorRow doctor) {
        super(owner, "Daily Availability - " + doctor.name, ModalityType.APPLICATION_MODAL);
        this.doctor = doctor;

        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Daily Availability: " + doctor.name, IconFactory.getDoctorsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Set clinician duty availability status");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        JPanel card = UIStyle.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        dateField = UIStyle.createStyledTextField(16);
        dateField.setText(LocalDate.now().toString());

        availableRadio = new JRadioButton("Available (Normal duty)");
        availableRadio.setFont(UIStyle.FONT_REGULAR);
        availableRadio.setOpaque(false);

        unavailableRadio = new JRadioButton("Unavailable (On leave / Off duty)");
        unavailableRadio.setFont(UIStyle.FONT_REGULAR);
        unavailableRadio.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(availableRadio);
        group.add(unavailableRadio);

        if ("AVAILABLE".equalsIgnoreCase(doctor.status)) {
            availableRadio.setSelected(true);
        } else {
            unavailableRadio.setSelected(true);
        }

        gbc.gridx = 0; gbc.gridy = 0;
        card.add(new JLabel("Target Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        card.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        card.add(new JLabel("Duty Status:"), gbc);
        JPanel radioPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        radioPanel.setOpaque(false);
        radioPanel.add(availableRadio);
        radioPanel.add(unavailableRadio);
        gbc.gridx = 1;
        card.add(radioPanel, gbc);

        root.add(card, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton("Update Status");
        saveBtn.addActionListener(e -> handleUpdate());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleUpdate() {
        doctor.status = availableRadio.isSelected() ? "AVAILABLE" : "UNAVAILABLE";
        this.updated = true;
        dispose();
    }

    public boolean isUpdated() {
        return updated;
    }
}
