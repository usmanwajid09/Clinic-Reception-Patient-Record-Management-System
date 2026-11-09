package com.clinic.ui.panels;

import com.clinic.model.Patient;
import com.clinic.model.Role;
import com.clinic.model.User;
import com.clinic.service.IAuthService;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.dialogs.PatientDialog;
import com.clinic.ui.dialogs.PatientHistoryDialog;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Presentation panel for Patient Intake, Directory Search, and History (FR1 - FR5).
 * Enforces role boundaries: Receptionists register and update demographics; Administrators audit records.
 */
public class PatientManagementPanel extends JPanel {
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    private final IAuthService authService;

    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private ModernButton registerBtn;
    private ModernButton updateBtn;

    public PatientManagementPanel(IPatientService patientService, IDoctorService doctorService, IAuthService authService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadPatients();
        applyRolePermissions();
    }

    public PatientManagementPanel(IPatientService patientService, IDoctorService doctorService) {
        this(patientService, doctorService, null);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Patient Registration & Medical Records", IconFactory.getPatientsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Register new patients, search directories, update permitted details, and review histories (FR1 - FR5)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Action controls
        JPanel actionsBar = new JPanel(new BorderLayout(16, 0));
        actionsBar.setOpaque(false);

        // Search Bar (FR2)
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍 Search (FR2):");
        searchIcon.setFont(UIStyle.FONT_BOLD);
        searchField = UIStyle.createStyledTextField(20);
        searchField.setToolTipText("Search by Patient ID, Name, or Phone (FR2)");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterPatients(); }
            public void removeUpdate(DocumentEvent e) { filterPatients(); }
            public void changedUpdate(DocumentEvent e) { filterPatients(); }
        });
        searchBox.add(searchIcon);
        searchBox.add(searchField);

        // Buttons
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        registerBtn = new ModernButton("+ Register New Patient (FR1)");
        registerBtn.addActionListener(e -> {
            PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), patientService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadPatients();
                ToastNotification.showSuccess(this, "Patient Registered", "Patient record saved successfully.");
            }
        });

        updateBtn = ModernButton.secondary("Update Demographics (FR3)");
        updateBtn.addActionListener(e -> handleUpdatePatient());

        ModernButton historyBtn = ModernButton.secondary("View Visit History (FR4)");
        historyBtn.addActionListener(e -> handleViewHistory());

        btnBox.add(historyBtn);
        btnBox.add(updateBtn);
        btnBox.add(registerBtn);

        actionsBar.add(searchBox, BorderLayout.WEST);
        actionsBar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(actionsBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Patient ID", "Full Name", "Date of Birth", "Gender", "Contact Number", "Address", "Emergency Contact"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        UIStyle.styleTable(patientTable);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void applyRolePermissions() {
        if (authService == null) return;
        User user = authService.getCurrentUser().orElse(null);
        if (user != null && user.getRole() == Role.ADMINISTRATOR) {
            // Admin is audit-only; Front desk registers patients
            registerBtn.setVisible(false);
            updateBtn.setVisible(false);
        } else {
            registerBtn.setVisible(true);
            updateBtn.setVisible(true);
        }
    }

    public void loadPatients() {
        applyRolePermissions();
        tableModel.setRowCount(0);
        List<Patient> patients = patientService.getAllPatients();
        for (Patient p : patients) {
            tableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getDateOfBirth().toString(),
                    p.getGender(),
                    p.getContactNumber(),
                    p.getAddress(),
                    p.getEmergencyContact()
            });
        }
    }

    private void filterPatients() {
        String query = searchField.getText().trim();
        List<Patient> results;
        if (query.isEmpty()) {
            results = patientService.getAllPatients();
        } else {
            results = patientService.searchPatients(query);
        }

        tableModel.setRowCount(0);
        for (Patient p : results) {
            tableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getDateOfBirth().toString(),
                    p.getGender(),
                    p.getContactNumber(),
                    p.getAddress(),
                    p.getEmergencyContact()
            });
        }
    }

    private void handleUpdatePatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select a patient from the table.");
            return;
        }

        String patientId = (String) tableModel.getValueAt(selectedRow, 0);
        patientService.getPatientById(patientId).ifPresent(patient -> {
            PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), patientService, patient);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadPatients();
                ToastNotification.showSuccess(this, "Demographics Updated", "Patient details updated for " + patient.getName());
            }
        });
    }

    private void handleViewHistory() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select a patient from the table.");
            return;
        }

        String patientId = (String) tableModel.getValueAt(selectedRow, 0);
        patientService.getPatientById(patientId).ifPresent(patient -> {
            PatientHistoryDialog dialog = new PatientHistoryDialog(SwingUtilities.getWindowAncestor(this),
                    patient, patientService, doctorService);
            dialog.setVisible(true);
        });
    }
}
