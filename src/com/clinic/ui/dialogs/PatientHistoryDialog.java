package com.clinic.ui.dialogs;

import com.clinic.model.Consultation;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.model.Prescription;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for viewing a patient's historical visits and clinical consultations (FR4, FR22, FR29).
 */
public class PatientHistoryDialog extends JDialog {
    private final Patient patient;
    private final IPatientService patientService;
    private final IDoctorService doctorService;

    public PatientHistoryDialog(Window owner, Patient patient, IPatientService patientService, IDoctorService doctorService) {
        super(owner, "Medical History - " + patient.getName() + " (" + patient.getPatientId() + ")", ModalityType.APPLICATION_MODAL);
        this.patient = patient;
        this.patientService = patientService;
        this.doctorService = doctorService;

        initComponents();
        setSize(850, 520);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header with Patient Info Banner
        JPanel infoPanel = UIStyle.createCard();
        infoPanel.setLayout(new GridLayout(2, 3, 10, 6));

        addInfoItem(infoPanel, "Patient ID", patient.getPatientId());
        addInfoItem(infoPanel, "Full Name", patient.getName());
        addInfoItem(infoPanel, "Age / Gender", patient.getAge() + " yrs / " + patient.getGender());
        addInfoItem(infoPanel, "Contact", patient.getContactNumber());
        addInfoItem(infoPanel, "Emergency Contact", patient.getEmergencyContact());
        addInfoItem(infoPanel, "Registered Since", DateTimeUtil.formatDateTime(patient.getRegisteredAt()));

        root.add(infoPanel, BorderLayout.NORTH);

        // Consultations Table
        List<Consultation> history = patientService.getPatientMedicalHistory(patient.getPatientId());

        String[] columns = {"Date & Time", "Doctor", "Department", "Symptoms", "Diagnosis", "Prescription"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Consultation c : history) {
            String doctorName = "N/A";
            String department = "N/A";
            Doctor doc = doctorService.getDoctorById(c.getDoctorId()).orElse(null);
            if (doc != null) {
                doctorName = doc.getName();
                department = doc.getDepartment().getName();
            }

            String rxSummary = "No Rx";
            Prescription rx = c.getPrescription();
            if (rx != null && !rx.isEmpty()) {
                rxSummary = rx.getItems().size() + " medication(s)";
            }

            model.addRow(new Object[]{
                    DateTimeUtil.formatDateTime(c.getConsultationTime()),
                    doctorName,
                    department,
                    c.getSymptoms(),
                    c.getDiagnosis(),
                    rxSummary
            });
        }

        JTable table = new JTable(model);
        UIStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableWrapper = new JPanel(new BorderLayout(0, 8));
        tableWrapper.setOpaque(false);
        JLabel historyLabel = new JLabel("Completed Consultations & Clinical Encounters (" + history.size() + "):");
        historyLabel.setFont(UIStyle.FONT_HEADER);
        historyLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        tableWrapper.add(historyLabel, BorderLayout.NORTH);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        root.add(tableWrapper, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);

        ModernButton closeBtn = ModernButton.secondary("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addInfoItem(JPanel panel, String label, String value) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(UIStyle.FONT_SMALL);
        l.setForeground(ThemeColors.TEXT_SECONDARY);

        JLabel v = new JLabel(value != null ? value : "N/A");
        v.setFont(UIStyle.FONT_BOLD);
        v.setForeground(ThemeColors.TEXT_PRIMARY);

        item.add(l, BorderLayout.NORTH);
        item.add(v, BorderLayout.CENTER);
        panel.add(item);
    }
}

