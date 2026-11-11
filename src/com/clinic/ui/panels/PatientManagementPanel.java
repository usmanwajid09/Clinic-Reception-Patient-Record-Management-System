package com.clinic.ui.panels;

import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.dialogs.PatientDialog;
import com.clinic.ui.dialogs.PatientHistoryDialog;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.PatientRow;
import com.clinic.ui.mock.MockClinicData.UiRole;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Presentation panel for Patient Registration Desk, Directory Search, and Visit History UI.
 */
public class PatientManagementPanel extends JPanel {
    private final UiRole currentRole;

    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private ModernButton registerBtn;
    private ModernButton updateBtn;

    public PatientManagementPanel(UiRole currentRole) {
        this.currentRole = currentRole != null ? currentRole : UiRole.RECEPTIONIST;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        applyRoleVisibility();
        loadPatients();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Patient Registration & Medical Records", IconFactory.getPatientsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Register walk-in patients, search records by ID, name, or phone, and inspect visit logs");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel actionsBar = new JPanel(new BorderLayout(16, 0));
        actionsBar.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel searchLabel = new JLabel("Search Directory:");
        searchLabel.setFont(UIStyle.FONT_BOLD);
        searchField = UIStyle.createStyledTextField(22);
        searchField.setToolTipText("Filter table by Patient ID, Name, or Phone");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterPatients(); }
            public void removeUpdate(DocumentEvent e) { filterPatients(); }
            public void changedUpdate(DocumentEvent e) { filterPatients(); }
        });
        searchBox.add(searchLabel);
        searchBox.add(searchField);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        registerBtn = new ModernButton("+ Register New Patient");
        registerBtn.addActionListener(e -> {
            PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadPatients();
                ToastNotification.showSuccess(this, "Patient Added", "Patient profile added to interface view.");
            }
        });

        updateBtn = ModernButton.secondary("Update Demographics");
        updateBtn.addActionListener(e -> handleUpdatePatient());

        ModernButton historyBtn = ModernButton.secondary("View Visit History");
        historyBtn.addActionListener(e -> handleViewHistory());

        btnBox.add(historyBtn);
        btnBox.add(updateBtn);
        btnBox.add(registerBtn);

        actionsBar.add(searchBox, BorderLayout.WEST);
        actionsBar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(actionsBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

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

    private void applyRoleVisibility() {
        boolean canEdit = currentRole != UiRole.ADMINISTRATOR;
        registerBtn.setVisible(canEdit);
        updateBtn.setVisible(canEdit);
    }

    public void loadPatients() {
        filterPatients();
    }

    private void filterPatients() {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        tableModel.setRowCount(0);

        for (PatientRow p : MockClinicData.getPatients()) {
            if (!query.isEmpty()) {
                boolean match = p.id.toLowerCase().contains(query)
                        || p.name.toLowerCase().contains(query)
                        || p.phone.toLowerCase().contains(query);
                if (!match) continue;
            }
            tableModel.addRow(new Object[]{p.id, p.name, p.dob, p.gender, p.phone, p.address, p.emergencyContact});
        }
    }

    private void handleUpdatePatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select a patient from the table.");
            return;
        }

        String patientId = (String) tableModel.getValueAt(selectedRow, 0);
        MockClinicData.findPatientById(patientId).ifPresent(patient -> {
            PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), patient);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadPatients();
                ToastNotification.showSuccess(this, "Demographics Updated", "Updated display details for " + patient.name);
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
        MockClinicData.findPatientById(patientId).ifPresent(patient -> {
            PatientHistoryDialog dialog = new PatientHistoryDialog(SwingUtilities.getWindowAncestor(this), patient);
            dialog.setVisible(true);
        });
    }
}
