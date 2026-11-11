package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.PatientRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal UI form dialog for Patient Registration and Demographic Updates.
 */
public class PatientDialog extends JDialog {
    private final PatientRow existingPatient;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField dobField;
    private JComboBox<String> genderCombo;
    private JTextField addressField;
    private JTextField contactField;
    private JTextField emergencyContactField;

    public PatientDialog(Window owner, PatientRow existingPatient) {
        super(owner, existingPatient == null ? "Register New Patient" : "Update Patient Information", ModalityType.APPLICATION_MODAL);
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

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(existingPatient == null ? "New Patient Registration" : "Edit Patient Profile", IconFactory.getPatientsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(existingPatient == null
                ? "Enter patient demographic and contact details"
                : "Update contact details and residential address");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        nameField = UIStyle.createStyledTextField(24);
        dobField = UIStyle.createStyledTextField(24);
        dobField.setText("1995-05-15");
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setFont(UIStyle.FONT_REGULAR);
        genderCombo.setBackground(Color.WHITE);
        addressField = UIStyle.createStyledTextField(24);
        contactField = UIStyle.createStyledTextField(24);
        emergencyContactField = UIStyle.createStyledTextField(24);

        int row = 0;
        addFormRow(form, gbc, row++, "Full Name *:", nameField);
        addFormRow(form, gbc, row++, "Date of Birth (YYYY-MM-DD):", dobField);
        addFormRow(form, gbc, row++, "Gender:", genderCombo);
        addFormRow(form, gbc, row++, "Residential Address:", addressField);
        addFormRow(form, gbc, row++, "Contact Number:", contactField);
        addFormRow(form, gbc, row++, "Emergency Contact:", emergencyContactField);

        if (existingPatient != null) {
            nameField.setEditable(false);
            nameField.setBackground(ThemeColors.BORDER_LIGHT);
            dobField.setEditable(false);
            dobField.setBackground(ThemeColors.BORDER_LIGHT);
            genderCombo.setEnabled(false);
        }

        root.add(form, BorderLayout.CENTER);

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
        nameField.setText(existingPatient.name);
        dobField.setText(existingPatient.dob);
        genderCombo.setSelectedItem(existingPatient.gender);
        addressField.setText(existingPatient.address);
        contactField.setText(existingPatient.phone);
        emergencyContactField.setText(existingPatient.emergencyContact);
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a patient name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String dob = dobField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String address = addressField.getText().trim();
        String phone = contactField.getText().trim();
        String emergency = emergencyContactField.getText().trim();

        if (existingPatient == null) {
            MockClinicData.getPatients().add(new PatientRow(
                    MockClinicData.nextPatientId(),
                    name,
                    dob.isEmpty() ? "1995-01-01" : dob,
                    gender != null ? gender : "Male",
                    phone.isEmpty() ? "0300-0000000" : phone,
                    address.isEmpty() ? "Lahore, Pakistan" : address,
                    emergency.isEmpty() ? "0301-0000000" : emergency
            ));
        } else {
            existingPatient.address = address;
            existingPatient.phone = phone;
            existingPatient.emergencyContact = emergency;
        }

        this.saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
