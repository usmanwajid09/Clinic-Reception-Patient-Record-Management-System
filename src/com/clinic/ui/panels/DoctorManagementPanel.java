package com.clinic.ui.panels;

import com.clinic.model.Department;
import com.clinic.model.Doctor;
import com.clinic.model.Role;
import com.clinic.model.User;
import com.clinic.service.IAuthService;
import com.clinic.service.IDoctorService;
import com.clinic.ui.animation.ToastNotification;
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
 * Doctor Directory and Administrative Management Panel (FR6 - FR10).
 * Implements role-based authority rules: Administrator configures doctors and schedules,
 * while front-desk staff inspects directories and sets daily duty availability.
 */
public class DoctorManagementPanel extends JPanel {
    private final IDoctorService doctorService;
    private final IAuthService authService;

    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> specializationFilter;

    private ModernButton registerBtn;
    private ModernButton scheduleBtn;
    private ModernButton availBtn;

    public DoctorManagementPanel(IDoctorService doctorService, IAuthService authService) {
        this.doctorService = doctorService;
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadDoctors();
        applyRolePermissions();
    }

    public DoctorManagementPanel(IDoctorService doctorService) {
        this(doctorService, null);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Clinician Directory & Schedule Operations");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Browse specialists (FR10), manage daily duty availability (FR9), and configure clinician profiles (FR6-8)");
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

        registerBtn = new ModernButton("+ Register Doctor (FR6)");
        registerBtn.addActionListener(e -> {
            DoctorDialog dialog = new DoctorDialog(SwingUtilities.getWindowAncestor(this), doctorService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshFilters();
                loadDoctors();
                ToastNotification.showSuccess(this, "Doctor Registered", "New clinician successfully onboarded.");
            }
        });

        scheduleBtn = ModernButton.secondary("Set Schedule (FR8)");
        scheduleBtn.addActionListener(e -> handleScheduleConfig());

        availBtn = ModernButton.secondary("Duty Status (FR9)");
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
        UIStyle.applyStatusBadge(doctorTable, 7);

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Enforces role-based visibility rules: Only Administrators can register doctors and set schedules.
     */
    public void applyRolePermissions() {
        if (authService != null) {
            User user = authService.getCurrentUser().orElse(null);
            boolean isAdmin = user != null && user.getRole() == Role.ADMINISTRATOR;
            registerBtn.setVisible(isAdmin);
            scheduleBtn.setVisible(isAdmin);
        }
    }

    public void loadDoctors() {
        applyRolePermissions();
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();

        List<Doctor> doctors = doctorService.getAllDoctors();
        for (Doctor doc : doctors) {
            boolean availableToday = doc.isAvailableOn(today);
            Department dept = doc.getDepartment();
            String deptName = dept != null ? dept.getName() : "Unassigned";

            String workingDaysStr = doc.getWorkingDays().stream()
                    .map(d -> d.name().substring(0, 3))
                    .collect(Collectors.joining(", "));

            tableModel.addRow(new Object[]{
                    doc.getDoctorId(),
                    doc.getName(),
                    deptName,
                    doc.getSpecialization(),
                    doc.getQualification(),
                    "Rs. " + String.format("%.2f", doc.getConsultationFee()),
                    workingDaysStr.isEmpty() ? "None" : workingDaysStr,
                    availableToday ? "AVAILABLE" : "UNAVAILABLE"
            });
        }
    }

    private void filterDoctors() {
        String selectedDept = (String) departmentFilter.getSelectedItem();
        String selectedSpec = (String) specializationFilter.getSelectedItem();

        String deptId = null;
        if (selectedDept != null && !selectedDept.equals("All Departments")) {
            for (Department d : doctorService.getAllDepartments()) {
                if (d.getName().equalsIgnoreCase(selectedDept)) {
                    deptId = d.getDepartmentId();
                    break;
                }
            }
        }

        String spec = null;
        if (selectedSpec != null && !selectedSpec.equals("All Specializations")) {
            spec = selectedSpec;
        }

        List<Doctor> filtered = doctorService.filterDoctors(deptId, spec);
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();

        for (Doctor doc : filtered) {
            boolean availableToday = doc.isAvailableOn(today);
            Department dept = doc.getDepartment();
            String deptName = dept != null ? dept.getName() : "Unassigned";

            String workingDaysStr = doc.getWorkingDays().stream()
                    .map(d -> d.name().substring(0, 3))
                    .collect(Collectors.joining(", "));

            tableModel.addRow(new Object[]{
                    doc.getDoctorId(),
                    doc.getName(),
                    deptName,
                    doc.getSpecialization(),
                    doc.getQualification(),
                    "Rs. " + String.format("%.2f", doc.getConsultationFee()),
                    workingDaysStr.isEmpty() ? "None" : workingDaysStr,
                    availableToday ? "AVAILABLE" : "UNAVAILABLE"
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
            ToastNotification.showWarning(this, "Selection Required", "Please select a clinician from the table first.");
            return;
        }
        String docId = (String) tableModel.getValueAt(row, 0);
        doctorService.getDoctorById(docId).ifPresent(doc -> {
            DoctorScheduleDialog dialog = new DoctorScheduleDialog(SwingUtilities.getWindowAncestor(this), doc, doctorService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadDoctors();
                ToastNotification.showSuccess(this, "Schedule Updated", "Working days & slots saved for " + doc.getName());
            }
        });
    }

    private void handleAvailabilityToggle() {
        int row = doctorTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select a clinician from the table first.");
            return;
        }
        String docId = (String) tableModel.getValueAt(row, 0);
        doctorService.getDoctorById(docId).ifPresent(doc -> {
            DoctorAvailabilityDialog dialog = new DoctorAvailabilityDialog(SwingUtilities.getWindowAncestor(this), doc, doctorService);
            dialog.setVisible(true);
            if (dialog.isUpdated()) {
                loadDoctors();
                ToastNotification.showSuccess(this, "Status Updated", "Duty availability adjusted for " + doc.getName());
            }
        });
    }
}
