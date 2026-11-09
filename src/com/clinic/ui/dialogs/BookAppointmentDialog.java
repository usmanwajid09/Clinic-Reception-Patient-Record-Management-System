package com.clinic.ui.dialogs;

import com.clinic.model.Appointment;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.model.TimeSlot;
import com.clinic.service.IAppointmentService;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
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
import java.util.List;

/**
 * Modal dialog for booking patient appointments with strict double-booking collision prevention (FR11, FR12, FR13).
 */
public class BookAppointmentDialog extends JDialog {
    private final IAppointmentService appointmentService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    private boolean booked = false;

    private JComboBox<Patient> patientCombo;
    private JComboBox<Doctor> doctorCombo;
    private JTextField dateField;
    private JComboBox<TimeSlot> slotCombo;
    private JTextField notesField;
    private JLabel statusFeedbackLabel;

    public BookAppointmentDialog(Window owner, IAppointmentService appointmentService,
                                 IPatientService patientService, IDoctorService doctorService) {
        super(owner, "Book Doctor Appointment", ModalityType.APPLICATION_MODAL);
        this.appointmentService = appointmentService;
        this.patientService = patientService;
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
        JLabel titleLabel = new JLabel("Schedule New Appointment");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Assign patient to an available doctor with conflict detection (FR11, FR13)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form Card
        JPanel card = UIStyle.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        List<Patient> patients = patientService.getAllPatients();
        patientCombo = new JComboBox<>(patients.toArray(new Patient[0]));
        patientCombo.setFont(UIStyle.FONT_REGULAR);
        patientCombo.setBackground(Color.WHITE);

        List<Doctor> doctors = doctorService.getAllDoctors();
        doctorCombo = new JComboBox<>(doctors.toArray(new Doctor[0]));
        doctorCombo.setFont(UIStyle.FONT_REGULAR);
        doctorCombo.setBackground(Color.WHITE);
        doctorCombo.addActionListener(e -> updateAvailableSlots());

        dateField = UIStyle.createStyledTextField(18);
        dateField.setText(LocalDate.now().toString());

        slotCombo = new JComboBox<>();
        slotCombo.setFont(UIStyle.FONT_REGULAR);
        slotCombo.setBackground(Color.WHITE);
        updateAvailableSlots();

        notesField = UIStyle.createStyledTextField(18);
        notesField.setToolTipText("e.g. Follow-up consultation, chest discomfort");

        statusFeedbackLabel = new JLabel(" ");
        statusFeedbackLabel.setFont(UIStyle.FONT_SMALL);
        statusFeedbackLabel.setForeground(ThemeColors.INFO);

        int row = 0;
        addFormRow(card, gbc, row++, "Select Patient (FR11) *:", patientCombo);
        addFormRow(card, gbc, row++, "Select Doctor *:", doctorCombo);
        addFormRow(card, gbc, row++, "Appointment Date (YYYY-MM-DD) *:", dateField);
        addFormRow(card, gbc, row++, "Consultation Time Slot *:", slotCombo);
        addFormRow(card, gbc, row++, "Chief Complaint / Notes:", notesField);

        root.add(card, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton confirmBtn = new ModernButton("Confirm Booking");
        confirmBtn.addActionListener(e -> handleBooking());

        footer.add(cancelBtn);
        footer.add(confirmBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIStyle.FONT_BOLD);
        lbl.setForeground(ThemeColors.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(input, gbc);
    }

    private void updateAvailableSlots() {
        slotCombo.removeAllItems();
        Doctor selectedDoctor = (Doctor) doctorCombo.getSelectedItem();
        if (selectedDoctor != null) {
            for (TimeSlot slot : selectedDoctor.getConsultationSlots()) {
                slotCombo.addItem(slot);
            }
        }
    }

    private void handleBooking() {
        Patient patient = (Patient) patientCombo.getSelectedItem();
        Doctor doctor = (Doctor) doctorCombo.getSelectedItem();
        TimeSlot slot = (TimeSlot) slotCombo.getSelectedItem();
        String dateStr = dateField.getText().trim();
        String notes = notesField.getText().trim();

        if (patient == null) {
            JOptionPane.showMessageDialog(this, "Please select a registered patient.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (doctor == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (slot == null) {
            JOptionPane.showMessageDialog(this, "Please select an available consultation time slot.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Invokes appointmentService which strictly enforces conflict prevention (FR13)
            Appointment appt = appointmentService.createAppointment(patient.getPatientId(), doctor.getDoctorId(), date, slot, notes);

            JOptionPane.showMessageDialog(this,
                    "Appointment Successfully Booked!\n"
                            + "Token Number: #" + appt.getTokenNumber() + " (FR12)\n"
                            + "Date: " + appt.getAppointmentDate() + "\n"
                            + "Time Slot: " + appt.getTimeSlot().getFormatted() + "\n"
                            + "Doctor: " + doctor.getName(),
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

            this.booked = true;
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Rejected", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isBooked() {
        return booked;
    }
}

