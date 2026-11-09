package com.clinic.ui.dialogs;

import com.clinic.config.AppConfig;
import com.clinic.model.Department;
import com.clinic.model.Doctor;
import com.clinic.model.TimeSlot;
import com.clinic.service.IDoctorService;
import com.clinic.service.ValidationException;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Modal dialog for registering a new doctor and associating with a department (FR6, FR7).
 */
public class DoctorDialog extends JDialog {
    private final IDoctorService doctorService;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField specializationField;
    private JTextField qualificationField;
    private JTextField feeField;
    private JComboBox<Department> departmentCombo;

    public DoctorDialog(Window owner, IDoctorService doctorService) {
        super(owner, "Register Doctor", ModalityType.APPLICATION_MODAL);
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
        JLabel titleLabel = new JLabel("Doctor Registration", IconFactory.getDoctorsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Enter physician credentials, consultation fee, and department (FR6, FR7)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form
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

        List<Department> departments = doctorService.getAllDepartments();
        departmentCombo = new JComboBox<>(departments.toArray(new Department[0]));
        departmentCombo.setFont(UIStyle.FONT_REGULAR);
        departmentCombo.setBackground(Color.WHITE);

        int row = 0;
        addFormRow(form, gbc, row++, "Doctor Full Name *:", nameField);
        addFormRow(form, gbc, row++, "Specialization *:", specializationField);
        addFormRow(form, gbc, row++, "Medical Qualifications *:", qualificationField);
        addFormRow(form, gbc, row++, "Department (FR7) *:", departmentCombo);
        addFormRow(form, gbc, row++, "Consultation Fee (" + AppConfig.CURRENCY_SYMBOL.trim() + ") *:", feeField);

        root.add(form, BorderLayout.CENTER);

        // Footer Actions
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
        String spec = specializationField.getText().trim();
        String qual = qualificationField.getText().trim();
        String feeStr = feeField.getText().trim();
        Department dept = (Department) departmentCombo.getSelectedItem();

        double fee;
        try {
            fee = Double.parseDouble(feeStr);
            if (fee < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive numeric fee.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Doctor doctor = doctorService.registerDoctor(name, spec, qual, fee, dept);
            // Assign standard default schedule (Mon-Fri 9am-12pm)
            doctorService.setDoctorSchedule(doctor.getDoctorId(),
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                    Arrays.asList(
                            TimeSlot.of(9, 0, 9, 30),
                            TimeSlot.of(9, 30, 10, 0),
                            TimeSlot.of(10, 0, 10, 30),
                            TimeSlot.of(10, 30, 11, 0),
                            TimeSlot.of(11, 0, 11, 30)
                    ));

            JOptionPane.showMessageDialog(this, "Doctor " + doctor.getName() + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.saved = true;
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
