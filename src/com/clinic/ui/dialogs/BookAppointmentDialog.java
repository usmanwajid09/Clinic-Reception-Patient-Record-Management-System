package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.AppointmentRow;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.mock.MockClinicData.PatientRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Modal UI form dialog for Booking an Appointment in the prototype interface.
 */
public class BookAppointmentDialog extends JDialog {
    private boolean booked = false;

    private JComboBox<PatientRow> patientCombo;
    private JComboBox<DoctorRow> doctorCombo;
    private JTextField dateField;
    private JComboBox<String> slotCombo;
    private JTextField notesField;

    public BookAppointmentDialog(Window owner) {
        super(owner, "Book New Appointment", ModalityType.APPLICATION_MODAL);

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
        JLabel titleLabel = new JLabel("Schedule New Appointment", IconFactory.getAppointmentsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Select patient, clinician, date, and consultation time slot");
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

        patientCombo = new JComboBox<>(MockClinicData.getPatients().toArray(new PatientRow[0]));
        patientCombo.setFont(UIStyle.FONT_REGULAR);
        patientCombo.setBackground(Color.WHITE);

        doctorCombo = new JComboBox<>(MockClinicData.getDoctors().toArray(new DoctorRow[0]));
        doctorCombo.setFont(UIStyle.FONT_REGULAR);
        doctorCombo.setBackground(Color.WHITE);
        doctorCombo.addActionListener(e -> updateSlots());

        dateField = UIStyle.createStyledTextField(18);
        dateField.setText(LocalDate.now().toString());

        slotCombo = new JComboBox<>();
        slotCombo.setFont(UIStyle.FONT_REGULAR);
        slotCombo.setBackground(Color.WHITE);
        updateSlots();

        notesField = UIStyle.createStyledTextField(18);

        int row = 0;
        addFormRow(card, gbc, row++, "Select Patient *:", patientCombo);
        addFormRow(card, gbc, row++, "Select Doctor *:", doctorCombo);
        addFormRow(card, gbc, row++, "Appointment Date (YYYY-MM-DD):", dateField);
        addFormRow(card, gbc, row++, "Consultation Time Slot:", slotCombo);
        addFormRow(card, gbc, row++, "Chief Complaint / Notes:", notesField);

        root.add(card, BorderLayout.CENTER);

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

    private void updateSlots() {
        slotCombo.removeAllItems();
        DoctorRow doc = (DoctorRow) doctorCombo.getSelectedItem();
        if (doc != null) {
            for (String s : doc.slots) {
                slotCombo.addItem(s);
            }
        }
    }

    private void handleBooking() {
        PatientRow patient = (PatientRow) patientCombo.getSelectedItem();
        DoctorRow doctor = (DoctorRow) doctorCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();
        if (patient == null || doctor == null || slot == null) return;

        String dateStr = dateField.getText().trim();
        if (dateStr.isEmpty()) dateStr = LocalDate.now().toString();

        MockClinicData.getAppointments().add(new AppointmentRow(
                MockClinicData.nextTokenNumber(),
                MockClinicData.nextAppointmentId(),
                dateStr,
                slot,
                patient.id,
                patient.name,
                doctor.id,
                doctor.name,
                doctor.department,
                "SCHEDULED",
                notesField.getText().trim()
        ));

        this.booked = true;
        dispose();
    }

    public boolean isBooked() {
        return booked;
    }
}
