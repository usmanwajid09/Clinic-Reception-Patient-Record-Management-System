package com.clinic.ui.panels;

import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.dialogs.DoctorAvailabilityDialog;
import com.clinic.ui.dialogs.DoctorDialog;
import com.clinic.ui.dialogs.DoctorScheduleDialog;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.mock.MockClinicData.UiRole;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Presentation panel for Clinician Directory, Schedule Configuration, and Duty Availability UI.
 */
public class DoctorManagementPanel extends JPanel {
    private final UiRole currentRole;

    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> specializationFilter;

    private ModernButton registerBtn;
    private ModernButton scheduleBtn;
    private ModernButton availBtn;

    public DoctorManagementPanel(UiRole currentRole) {
        this.currentRole = currentRole != null ? currentRole : UiRole.ADMINISTRATOR;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        applyRoleVisibility();
        loadDoctors();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Clinician Directory & Schedule Operations", IconFactory.getDoctorsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Browse specialists by department, toggle duty status, and configure clinician timetables");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBox.setOpaque(false);

        JLabel deptLabel = new JLabel("Department:");
        deptLabel.setFont(UIStyle.FONT_BOLD);
        departmentFilter = new JComboBox<>();
        departmentFilter.setFont(UIStyle.FONT_REGULAR);
        departmentFilter.setBackground(Color.WHITE);
        departmentFilter.addItem("All Departments");
        for (String d : MockClinicData.DEPARTMENTS) {
            departmentFilter.addItem(d);
        }
        departmentFilter.addActionListener(e -> filterDoctors());

        JLabel specLabel = new JLabel("Specialization:");
        specLabel.setFont(UIStyle.FONT_BOLD);
        specializationFilter = new JComboBox<>();
        specializationFilter.setFont(UIStyle.FONT_REGULAR);
        specializationFilter.setBackground(Color.WHITE);
        populateSpecFilter();
        specializationFilter.addActionListener(e -> filterDoctors());

        filterBox.add(deptLabel);
        filterBox.add(departmentFilter);
        filterBox.add(specLabel);
        filterBox.add(specializationFilter);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        registerBtn = new ModernButton("+ Register Doctor");
        registerBtn.addActionListener(e -> {
            DoctorDialog dialog = new DoctorDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                populateSpecFilter();
                loadDoctors();
                ToastNotification.showSuccess(this, "Doctor Added", "New clinician card added to directory view.");
            }
        });

        scheduleBtn = ModernButton.secondary("Configure Schedule");
        scheduleBtn.addActionListener(e -> handleScheduleConfig());

        availBtn = ModernButton.secondary("Toggle Availability");
        availBtn.addActionListener(e -> handleAvailabilityToggle());

        btnBox.add(availBtn);
        btnBox.add(scheduleBtn);
        btnBox.add(registerBtn);

        toolbar.add(filterBox, BorderLayout.WEST);
        toolbar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

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

    private void populateSpecFilter() {
        if (specializationFilter == null) return;
        specializationFilter.removeAllItems();
        specializationFilter.addItem("All Specializations");
        MockClinicData.getDoctors().stream()
                .map(d -> d.specialization)
                .distinct()
                .sorted()
                .forEach(specializationFilter::addItem);
    }

    private void applyRoleVisibility() {
        boolean isAdmin = currentRole == UiRole.ADMINISTRATOR;
        registerBtn.setVisible(isAdmin);
        scheduleBtn.setVisible(isAdmin);
    }

    public void loadDoctors() {
        filterDoctors();
    }

    private void filterDoctors() {
        String selectedDept = (String) departmentFilter.getSelectedItem();
        String selectedSpec = (String) specializationFilter.getSelectedItem();

        tableModel.setRowCount(0);
        for (DoctorRow doc : MockClinicData.getDoctors()) {
            if (selectedDept != null && !"All Departments".equals(selectedDept) && !doc.department.equalsIgnoreCase(selectedDept)) {
                continue;
            }
            if (selectedSpec != null && !"All Specializations".equals(selectedSpec) && !doc.specialization.equalsIgnoreCase(selectedSpec)) {
                continue;
            }
            tableModel.addRow(new Object[]{
                    doc.id,
                    doc.name,
                    doc.department,
                    doc.specialization,
                    doc.qualification,
                    UIBranding.CURRENCY_SYMBOL + String.format("%.2f", doc.fee),
                    doc.workingDays,
                    doc.status
            });
        }
    }

    private void handleScheduleConfig() {
        int row = doctorTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select a clinician from the table first.");
            return;
        }
        String docId = (String) tableModel.getValueAt(row, 0);
        MockClinicData.findDoctorById(docId).ifPresent(doc -> {
            DoctorScheduleDialog dialog = new DoctorScheduleDialog(SwingUtilities.getWindowAncestor(this), doc);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadDoctors();
                ToastNotification.showSuccess(this, "Schedule Updated", "Working days updated for " + doc.name);
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
        MockClinicData.findDoctorById(docId).ifPresent(doc -> {
            DoctorAvailabilityDialog dialog = new DoctorAvailabilityDialog(SwingUtilities.getWindowAncestor(this), doc);
            dialog.setVisible(true);
            if (dialog.isUpdated()) {
                loadDoctors();
                ToastNotification.showSuccess(this, "Status Updated", "Duty status adjusted for " + doc.name);
            }
        });
    }
}
