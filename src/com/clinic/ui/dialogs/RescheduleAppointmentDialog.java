package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.AppointmentRow;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal UI dialog for updating an appointment date and time slot visually.
 */
public class RescheduleAppointmentDialog extends JDialog {
    private final AppointmentRow appointment;
    private boolean rescheduled = false;

    private JTextField dateField;
    private JComboBox<String> slotCombo;

    public RescheduleAppointmentDialog(Window owner, AppointmentRow appointment) {
        super(owner, "Reschedule Appointment #" + appointment.apptId, ModalityType.APPLICATION_MODAL);
        this.appointment = appointment;

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
        JLabel titleLabel = new JLabel("Reschedule Appointment", IconFactory.getAppointmentsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Choose an updated date and consultation time slot");
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
        dateField.setText(appointment.date);

        DoctorRow doc = MockClinicData.findDoctorById(appointment.doctorId).orElse(null);
        java.util.List<String> slots = doc != null ? doc.slots : MockClinicData.STANDARD_SLOTS;
        slotCombo = new JComboBox<>(slots.toArray(new String[0]));
        slotCombo.setSelectedItem(appointment.timeSlot);
        slotCombo.setFont(UIStyle.FONT_REGULAR);
        slotCombo.setBackground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0;
        card.add(new JLabel("New Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        card.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        card.add(new JLabel("New Consultation Slot:"), gbc);
        gbc.gridx = 1;
        card.add(slotCombo, gbc);

        root.add(card, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton("Save Reschedule");
        saveBtn.addActionListener(e -> handleSave());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleSave() {
        String newDate = dateField.getText().trim();
        String newSlot = (String) slotCombo.getSelectedItem();
        if (!newDate.isEmpty()) appointment.date = newDate;
        if (newSlot != null) appointment.timeSlot = newSlot;
        this.rescheduled = true;
        dispose();
    }

    public boolean isRescheduled() {
        return rescheduled;
    }
}
