package com.clinic.ui.dialogs;

import com.clinic.model.Doctor;
import com.clinic.service.IDoctorService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for marking a doctor as Available or Unavailable for a specified day (FR9).
 */
public class DoctorAvailabilityDialog extends JDialog {
    private final Doctor doctor;
    private final IDoctorService doctorService;
    private boolean updated = false;

    private JTextField dateField;
    private JRadioButton availableRadio;
    private JRadioButton unavailableRadio;

    public DoctorAvailabilityDialog(Window owner, Doctor doctor, IDoctorService doctorService) {
        super(owner, "Daily Availability - " + doctor.getName(), ModalityType.APPLICATION_MODAL);
        this.doctor = doctor;
        this.doctorService = doctorService;

        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Daily Availability: " + doctor.getName());
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Mark clinician as Available or Unavailable for a specific date (FR9)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form
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

        unavailableRadio = new JRadioButton("Unavailable (Leave / Emergency absence)");
        unavailableRadio.setFont(UIStyle.FONT_REGULAR);
        unavailableRadio.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(availableRadio);
        group.add(unavailableRadio);

        // Pre-check based on today's availability
        if (doctor.isAvailableOn(LocalDate.now())) {
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

        // Footer Actions
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
        String dateStr = dateField.getText().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format. Use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean available = availableRadio.isSelected();
        doctorService.setDoctorAvailabilityForDate(doctor.getDoctorId(), date, available);

        JOptionPane.showMessageDialog(this,
                "Doctor " + doctor.getName() + " marked as " + (available ? "AVAILABLE" : "UNAVAILABLE") + " for " + date + " (FR9).",
                "Status Updated", JOptionPane.INFORMATION_MESSAGE);

        this.updated = true;
        dispose();
    }

    public boolean isUpdated() {
        return updated;
    }
}
