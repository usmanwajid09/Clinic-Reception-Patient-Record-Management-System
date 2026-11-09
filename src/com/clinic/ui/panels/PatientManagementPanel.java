package com.clinic.ui.panels;

import com.clinic.model.Patient;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.dialogs.PatientDialog;
import com.clinic.ui.dialogs.PatientHistoryDialog;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Presentation panel for Patient Registration and Directory Management (FR1, FR2, FR3, FR4, FR5).
 */
public class PatientManagementPanel extends JPanel {
    private final IPatientService patientService;
    private final IDoctorService doctorService;

    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private StatCard totalPatientsCard;

    public PatientManagementPanel(IPatientService patientService, IDoctorService doctorService) {
        this.patientService = patientService;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadPatients();
    }

    private void initComponents() {
        // Top Toolbar & Stats
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Patient Registration & Medical Records");
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

        ModernButton registerBtn = new ModernButton("+ Register New Patient (FR1)");
        registerBtn.addActionListener(e -> {
            PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), patientService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadPatients();
            }
        });

        ModernButton editBtn = ModernButton.secondary("Edit Details (FR3)");
        editBtn.addActionListener(e -> handleEditPatient());

        ModernButton historyBtn = ModernButton.secondary("View History (FR4)");
        historyBtn.addActionListener(e -> handleViewHistory());

        btnBox.add(historyBtn);
        btnBox.add(editBtn);
        btnBox.add(registerBtn);

        actionsBar.add(searchBox, BorderLayout.WEST);
        actionsBar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(actionsBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Patient ID", "Name", "Age / Gender", "Date of Birth", "Contact Number", "Emergency Contact", "Address"};
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

    public void loadPatients() {
        tableModel.setRowCount(0);
        List<Patient> patients = patientService.getAllPatients();
        for (Patient p : patients) {
            tableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getAge() + " yrs / " + p.getGender(),
                    DateTimeUtil.formatDate(p.getDateOfBirth()),
                    p.getContactNumber(),
                    p.getEmergencyContact(),
                    p.getAddress()
            });
        }
    }

    private void filterPatients() {
        String query = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Patient> filtered = patientService.searchPatients(query);
        for (Patient p : filtered) {
            tableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getAge() + " yrs / " + p.getGender(),
                    DateTimeUtil.formatDate(p.getDateOfBirth()),
                    p.getContactNumber(),
                    p.getEmergencyContact(),
                    p.getAddress()
            });
        }
    }

    private void handleEditPatient() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String patientId = (String) tableModel.getValueAt(row, 0);
        patientService.getPatientById(patientId).ifPresent(patient -> {
            PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), patientService, patient);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadPatients();
            }
        });
    }

    private void handleViewHistory() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to view history.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String patientId = (String) tableModel.getValueAt(row, 0);
        patientService.getPatientById(patientId).ifPresent(patient -> {
            PatientHistoryDialog dialog = new PatientHistoryDialog(SwingUtilities.getWindowAncestor(this), patient, patientService, doctorService);
            dialog.setVisible(true);
        });
    }
}
