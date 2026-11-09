package com.clinic.ui.panels;

import com.clinic.config.AppConfig;
import com.clinic.model.Department;
import com.clinic.model.Doctor;
import com.clinic.service.IDoctorService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.dialogs.DoctorAvailabilityDialog;
import com.clinic.ui.dialogs.DoctorDialog;
import com.clinic.ui.dialogs.DoctorScheduleDialog;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presentation panel for Doctor Directory and Schedule Operations (FR6, FR7, FR8, FR9, FR10).
 */
public class DoctorManagementPanel extends JPanel {
    private final IDoctorService doctorService;

    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> specializationFilter;

    public DoctorManagementPanel(IDoctorService doctorService) {
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadDoctors();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Clinician & Schedule Management");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Register doctors, configure schedules & slots, set daily duty availability, and filter directories (FR6 - FR10)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Filter & Action Toolbar (FR10)
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBox.setOpaque(false);

        JLabel deptLabel = new JLabel("Department (FR10):");
        deptLabel.setFont(UIStyle.FONT_BOLD);
        departmentFilter = new JComboBox<>();
        departmentFilter.setFont(UIStyle.FONT_REGULAR);
        departmentFilter.setBackground(Color.WHITE);
        departmentFilter.addItem("All Departments");
        for (Department d : doctorService.getAllDepartments()) {
            departmentFilter.addItem(d.getName());
        }
        departmentFilter.addActionListener(e -> filterDoctors());

        JLabel specLabel = new JLabel("Specialization (FR10):");
        specLabel.setFont(UIStyle.FONT_BOLD);
        specializationFilter = new JComboBox<>();
        specializationFilter.setFont(UIStyle.FONT_REGULAR);
        specializationFilter.setBackground(Color.WHITE);
        specializationFilter.addItem("All Specializations");
        for (String spec : doctorService.getAllDoctors().stream().map(Doctor::getSpecialization).distinct().sorted().toList()) {
            specializationFilter.addItem(spec);
        }
        specializationFilter.addActionListener(e -> filterDoctors());

        filterBox.add(deptLabel);
        filterBox.add(departmentFilter);
        filterBox.add(specLabel);
        filterBox.add(specializationFilter);

        // Action Buttons
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        ModernButton registerBtn = new ModernButton("+ Register Doctor (FR6)");
        registerBtn.addActionListener(e -> {
            DoctorDialog dialog = new DoctorDialog(SwingUtilities.getWindowAncestor(this), doctorService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshFilters();
                loadDoctors();
            }
        });

        ModernButton scheduleBtn = ModernButton.secondary("Set Schedule (FR8)");
        scheduleBtn.addActionListener(e -> handleScheduleConfig());

        ModernButton availBtn = ModernButton.secondary("Duty Status (FR9)");
        availBtn.addActionListener(e -> handleAvailabilityToggle());

        btnBox.add(availBtn);
        btnBox.add(scheduleBtn);
        btnBox.add(registerBtn);

        toolbar.add(filterBox, BorderLayout.WEST);
        toolbar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Doctor ID", "Doctor Name", "Department", "Specialization", "Qualifications", "Consultation Fee", "Working Days", "Today's Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorTable = new JTable(tableModel);
        UIStyle.styleTable(doctorTable);

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadDoctors() {
        filterDoctors();
    }

    private void filterDoctors() {
        String dept = (String) departmentFilter.getSelectedItem();
        String spec = (String) specializationFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<Doctor> doctors = doctorService.filterDoctors(dept, spec);
        LocalDate today = LocalDate.now();

        for (Doctor doc : doctors) {
            String workingDaysStr = doc.getWorkingDays().stream()
                    .map(d -> d.toString().substring(0, 3))
                    .collect(Collectors.joining(", "));

            boolean availableToday = doc.isAvailableOn(today);
            String statusStr = availableToday ? "AVAILABLE" : "UNAVAILABLE";

            tableModel.addRow(new Object[]{
                    doc.getDoctorId(),
                    doc.getName(),
                    doc.getDepartment().getName(),
                    doc.getSpecialization(),
                    doc.getQualification(),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", doc.getConsultationFee()),
                    workingDaysStr.isEmpty() ? "Not Set" : workingDaysStr,
                    statusStr
            });
        }
    }

    private void refreshFilters() {
        departmentFilter.removeAllItems();
        departmentFilter.addItem("All Departments");
        for (Department d : doctorService.getAllDepartments()) {
            departmentFilter.addItem(d.getName());
        }

        specializationFilter.removeAllItems();
        specializationFilter.addItem("All Specializations");
        for (String spec : doctorService.getAllDoctors().stream().map(Doctor::getSpecialization).distinct().sorted().toList()) {
            specializationFilter.addItem(spec);
        }
    }

    private void handleScheduleConfig() {
        int row = doctorTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to configure schedule.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String docId = (String) tableModel.getValueAt(row, 0);
        doctorService.getDoctorById(docId).ifPresent(doc -> {
            DoctorScheduleDialog dialog = new DoctorScheduleDialog(SwingUtilities.getWindowAncestor(this), doc, doctorService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadDoctors();
            }
        });
    }

    private void handleAvailabilityToggle() {
        int row = doctorTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to update daily availability.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String docId = (String) tableModel.getValueAt(row, 0);
        doctorService.getDoctorById(docId).ifPresent(doc -> {
            DoctorAvailabilityDialog dialog = new DoctorAvailabilityDialog(SwingUtilities.getWindowAncestor(this), doc, doctorService);
            dialog.setVisible(true);
            if (dialog.isUpdated()) {
                loadDoctors();
            }
        });
    }
}
