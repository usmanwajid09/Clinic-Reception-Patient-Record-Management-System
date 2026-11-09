package com.clinic.ui.dialogs;

import com.clinic.model.Appointment;
import com.clinic.model.Doctor;
import com.clinic.model.TimeSlot;
import com.clinic.service.IAppointmentService;
import com.clinic.service.IDoctorService;
import com.clinic.service.ValidationException;
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
 * Modal dialog for rescheduling an appointment (FR14).
 * Performs double-booking verification on the new slot.
 */
public class RescheduleAppointmentDialog extends JDialog {
    private final Appointment appointment;
    private final IAppointmentService appointmentService;
    private final IDoctorService doctorService;
    private boolean rescheduled = false;

    private JTextField dateField;
    private JComboBox<TimeSlot> slotCombo;

    public RescheduleAppointmentDialog(Window owner, Appointment appointment,
                                       IAppointmentService appointmentService, IDoctorService doctorService) {
        super(owner, "Reschedule Appointment #" + appointment.getAppointmentId(), ModalityType.APPLICATION_MODAL);
        this.appointment = appointment;
        this.appointmentService = appointmentService;
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
        JLabel titleLabel = new JLabel("Reschedule Appointment");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Select a new date and consultation time slot (FR14)");
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

        dateField = UIStyle.createStyledTextField(18);
        dateField.setText(appointment.getAppointmentDate().toString());

        slotCombo = new JComboBox<>();
        slotCombo.setFont(UIStyle.FONT_REGULAR);
        slotCombo.setBackground(Color.WHITE);

        Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId()).orElse(null);
        if (doctor != null) {
            for (TimeSlot slot : doctor.getConsultationSlots()) {
                slotCombo.addItem(slot);
            }
            slotCombo.setSelectedItem(appointment.getTimeSlot());
        }

        gbc.gridx = 0; gbc.gridy = 0;
        card.add(new JLabel("New Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        card.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        card.add(new JLabel("New Time Slot:"), gbc);
        gbc.gridx = 1;
        card.add(slotCombo, gbc);

        root.add(card, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton("Save Reschedule");
        saveBtn.addActionListener(e -> handleReschedule());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleReschedule() {
        String dateStr = dateField.getText().trim();
        TimeSlot slot = (TimeSlot) slotCombo.getSelectedItem();

        if (slot == null) {
            JOptionPane.showMessageDialog(this, "Please select a time slot.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate newDate;
        try {
            newDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            appointmentService.rescheduleAppointment(appointment.getAppointmentId(), newDate, slot);
            JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.rescheduled = true;
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reschedule Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isRescheduled() {
        return rescheduled;
    }
}
