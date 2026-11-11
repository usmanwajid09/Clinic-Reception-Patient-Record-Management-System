package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal UI dialog for Registering a New Clinician Profile.
 */
public class DoctorDialog extends JDialog {
    private boolean saved = false;

    private JTextField nameField;
    private JTextField specializationField;
    private JTextField qualificationField;
    private JComboBox<String> departmentCombo;
    private JTextField feeField;

    public DoctorDialog(Window owner) {
        super(owner, "Register Doctor", ModalityType.APPLICATION_MODAL);

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
        JLabel titleLabel = new JLabel("Doctor Registration", IconFactory.getDoctorsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Enter physician credentials, consultation fee, and department");
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
        nameField.setText("Dr. ");
        specializationField = UIStyle.createStyledTextField(24);
        qualificationField = UIStyle.createStyledTextField(24);
        feeField = UIStyle.createStyledTextField(24);
        feeField.setText("2000.00");

        departmentCombo = new JComboBox<>(MockClinicData.DEPARTMENTS.toArray(new String[0]));
        departmentCombo.setFont(UIStyle.FONT_REGULAR);
        departmentCombo.setBackground(Color.WHITE);

        int row = 0;
        addFormRow(form, gbc, row++, "Doctor Full Name *:", nameField);
        addFormRow(form, gbc, row++, "Specialization:", specializationField);
        addFormRow(form, gbc, row++, "Medical Qualifications:", qualificationField);
        addFormRow(form, gbc, row++, "Department:", departmentCombo);
        addFormRow(form, gbc, row++, "Consultation Fee (" + UIBranding.CURRENCY_SYMBOL.trim() + "):", feeField);

        root.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton("Register Doctor");
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

    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty() || name.equals("Dr.")) {
            JOptionPane.showMessageDialog(this, "Please enter a clinician name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String dept = (String) departmentCombo.getSelectedItem();
        String spec = specializationField.getText().trim();
        String qual = qualificationField.getText().trim();
        double fee = 2000.0;
        try {
            fee = Double.parseDouble(feeField.getText().trim());
        } catch (NumberFormatException ignored) {}

        MockClinicData.getDoctors().add(new DoctorRow(
                MockClinicData.nextDoctorId(),
                name,
                dept != null ? dept : "General Medicine",
                spec.isEmpty() ? (dept != null ? dept : "General") : spec,
                qual.isEmpty() ? "MBBS, FCPS" : qual,
                fee,
                "MON, TUE, WED, THU, FRI",
                "AVAILABLE",
                MockClinicData.STANDARD_SLOTS.subList(0, 6)
        ));

        this.saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
