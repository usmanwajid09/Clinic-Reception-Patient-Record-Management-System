package com.clinic.ui.dialogs;

import com.clinic.model.Patient;
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

/**
 * Modal dialog for Patient Registration (FR1) and permitted information updates (FR3).
 * Validates mandatory fields before saving (FR5).
 */
public class PatientDialog extends JDialog {
    private final IPatientService patientService;
    private final Patient existingPatient; // If null, mode is ADD; if non-null, mode is EDIT
    private boolean saved = false;

    private JTextField nameField;
    private JTextField dobField; // YYYY-MM-DD
    private JComboBox<String> genderCombo;
    private JTextField addressField;
    private JTextField contactField;
    private JTextField emergencyContactField;

    public PatientDialog(Window owner, IPatientService patientService, Patient existingPatient) {
        super(owner, existingPatient == null ? "Register New Patient" : "Update Patient Information", ModalityType.APPLICATION_MODAL);
        this.patientService = patientService;
        this.existingPatient = existingPatient;

        initComponents();
        if (existingPatient != null) {
            populateFields();
        }
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
        JLabel titleLabel = new JLabel(existingPatient == null ? "New Patient Registration" : "Edit Patient Profile");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(existingPatient == null
                ? "Enter mandatory patient demographic and contact details (FR1, FR5)"
                : "Update contact details and residential address (FR3)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        nameField = UIStyle.createStyledTextField(24);
        dobField = UIStyle.createStyledTextField(24);
        dobField.setToolTipText("Format: YYYY-MM-DD (e.g. 1990-05-20)");
        if (existingPatient == null) {
            dobField.setText("1995-01-01");
        }

        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setFont(UIStyle.FONT_REGULAR);
        genderCombo.setBackground(Color.WHITE);

        addressField = UIStyle.createStyledTextField(24);
        addressField.setToolTipText("e.g. House 14, Street 5, Gulberg III, Lahore");
        contactField = UIStyle.createStyledTextField(24);
        contactField.setToolTipText("e.g. 0300-1234567");
        emergencyContactField = UIStyle.createStyledTextField(24);
        emergencyContactField.setToolTipText("e.g. Tariq Khan (Father - 0300-7654321)");

        int row = 0;
        addFormRow(form, gbc, row++, "Full Name *:", nameField);
        addFormRow(form, gbc, row++, "Date of Birth (YYYY-MM-DD) *:", dobField);
        addFormRow(form, gbc, row++, "Gender *:", genderCombo);
        addFormRow(form, gbc, row++, "Residential Address *:", addressField);
        addFormRow(form, gbc, row++, "Contact Phone *:", contactField);
        addFormRow(form, gbc, row++, "Emergency Contact *:", emergencyContactField);

        root.add(form, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton(existingPatient == null ? "Register Patient" : "Save Changes");
        saveBtn.addActionListener(e -> handleSave());

        footer.add(cancelBtn);
        footer.add(saveBtn);
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

    private void populateFields() {
        nameField.setText(existingPatient.getName());
        dobField.setText(existingPatient.getDateOfBirth().toString());
        genderCombo.setSelectedItem(existingPatient.getGender());
        addressField.setText(existingPatient.getAddress());
        contactField.setText(existingPatient.getContactNumber());
        emergencyContactField.setText(existingPatient.getEmergencyContact());
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String dobStr = dobField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String address = addressField.getText().trim();
        String contact = contactField.getText().trim();
        String emergency = emergencyContactField.getText().trim();

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid Date of Birth format. Please use YYYY-MM-DD (e.g. 1992-08-25).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (existingPatient == null) {
                // Register new (FR1, FR5)
                patientService.registerPatient(name, dob, gender, address, contact, emergency);
                JOptionPane.showMessageDialog(this, "Patient successfully registered!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update permitted information (FR3, FR5)
                patientService.updatePatient(existingPatient.getPatientId(), name, dob, gender, address, contact, emergency);
                JOptionPane.showMessageDialog(this, "Patient details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            this.saved = true;
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Failed (FR5)", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
