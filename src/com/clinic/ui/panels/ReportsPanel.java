package com.clinic.ui.panels;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.*;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/**
 * Presentation panel for Clinical, Operational, and Revenue Analytics Tabbed Views.
 * Filters report tabs per role (Administrator: all 4; Receptionist: Appointments, Visit History, Revenue).
 */
public class ReportsPanel extends JPanel {
    private final UiRole currentRole;

    private JTextField startDateField;
    private JTextField endDateField;

    private DefaultTableModel appointmentModel;
    private DefaultTableModel doctorModel;
    private DefaultTableModel patientHistoryModel;
    private DefaultTableModel revenueModel;

    private JComboBox<PatientRow> patientSelector;

    public ReportsPanel() {
        this(UiRole.ADMINISTRATOR);
    }

    public ReportsPanel(UiRole currentRole) {
        this.currentRole = currentRole != null ? currentRole : UiRole.ADMINISTRATOR;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        runReports();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Clinical Analytics & Executive Reporting", IconFactory.getReportsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        String subtitle = currentRole == UiRole.RECEPTIONIST
                ? "Inspect appointment summaries, patient visit-history logs, and billing revenue tables"
                : "Inspect appointment statistics, doctor productivity, patient visits, and revenue tables";
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel filterBar = UIStyle.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        JLabel startLbl = new JLabel("Start Date (YYYY-MM-DD):");
        startLbl.setFont(UIStyle.FONT_BOLD);
        startDateField = UIStyle.createStyledTextField(10);
        startDateField.setText(LocalDate.now().minusDays(7).toString());

        JLabel endLbl = new JLabel("End Date (YYYY-MM-DD):");
        endLbl.setFont(UIStyle.FONT_BOLD);
        endDateField = UIStyle.createStyledTextField(10);
        endDateField.setText(LocalDate.now().toString());

        ModernButton runBtn = new ModernButton("Refresh View");
        runBtn.addActionListener(e -> runReports());

        ModernButton todayBtn = ModernButton.secondary("Today");
        todayBtn.addActionListener(e -> {
            startDateField.setText(LocalDate.now().toString());
            endDateField.setText(LocalDate.now().toString());
            runReports();
        });

        ModernButton weekBtn = ModernButton.secondary("Last 7 Days");
        weekBtn.addActionListener(e -> {
            startDateField.setText(LocalDate.now().minusDays(7).toString());
            endDateField.setText(LocalDate.now().toString());
            runReports();
        });

        filterBar.add(startLbl);
        filterBar.add(startDateField);
        filterBar.add(endLbl);
        filterBar.add(endDateField);
        filterBar.add(runBtn);
        filterBar.add(todayBtn);
        filterBar.add(weekBtn);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(filterBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_BOLD);

        tabbedPane.addTab("Appointment Reports", createAppointmentReportTab());
        if (currentRole == UiRole.ADMINISTRATOR) {
            tabbedPane.addTab("Doctor Performance", createDoctorReportTab());
        }
        tabbedPane.addTab("Patient Visit History", createPatientHistoryTab());
        tabbedPane.addTab("Revenue & Billing", createRevenueReportTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createAppointmentReportTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Appt ID", "Date", "Slot", "Patient", "Doctor", "Department", "Status", "Notes"};
        appointmentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(appointmentModel);
        UIStyle.styleTable(table);
        UIStyle.applyStatusBadge(table, 6);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDoctorReportTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Doctor ID", "Doctor Name", "Department", "Specialization", "Consultations", "Unique Patients", "Fee Summary"};
        doctorModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(doctorModel);
        UIStyle.styleTable(table);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPatientHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel selectBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectBar.setOpaque(false);
        JLabel pLabel = new JLabel("Select Patient:");
        pLabel.setFont(UIStyle.FONT_BOLD);

        patientSelector = new JComboBox<>(MockClinicData.getPatients().toArray(new PatientRow[0]));
        patientSelector.setFont(UIStyle.FONT_REGULAR);
        patientSelector.setBackground(Color.WHITE);
        patientSelector.addActionListener(e -> loadSelectedPatientHistory());

        selectBar.add(pLabel);
        selectBar.add(patientSelector);
        panel.add(selectBar, BorderLayout.NORTH);

        String[] cols = {"Consultation Date", "Attending Doctor", "Department", "Symptoms", "Diagnosis", "Prescription Summary"};
        patientHistoryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(patientHistoryModel);
        UIStyle.styleTable(table);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRevenueReportTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Invoice ID", "Date", "Consultation Ref", "Doctor", "Doctor Fee", "Clinic Charge", "Total", "Status", "Method"};
        revenueModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(revenueModel);
        UIStyle.styleTable(table);
        UIStyle.applyStatusBadge(table, 7);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void runReports() {
        appointmentModel.setRowCount(0);
        for (AppointmentRow a : MockClinicData.getAppointments()) {
            appointmentModel.addRow(new Object[]{a.apptId, a.date, a.timeSlot, a.patientName, a.doctorName, a.department, a.status, a.notes});
        }

        if (doctorModel != null) {
            doctorModel.setRowCount(0);
            for (DoctorRow d : MockClinicData.getDoctors()) {
                long count = MockClinicData.getAppointments().stream().filter(a -> a.doctorId.equals(d.id)).count();
                doctorModel.addRow(new Object[]{
                        d.id,
                        d.name,
                        d.department,
                        d.specialization,
                        count,
                        count,
                        UIBranding.CURRENCY_SYMBOL + String.format("%.2f", d.fee * Math.max(1, count))
                });
            }
        }

        loadSelectedPatientHistory();

        revenueModel.setRowCount(0);
        for (BillRow b : MockClinicData.getBills()) {
            revenueModel.addRow(new Object[]{
                    b.billId,
                    b.date,
                    b.consultationId,
                    b.doctorName,
                    UIBranding.CURRENCY_SYMBOL + String.format("%.2f", b.doctorFee),
                    UIBranding.CURRENCY_SYMBOL + String.format("%.2f", b.clinicCharge),
                    UIBranding.CURRENCY_SYMBOL + String.format("%.2f", b.totalAmount),
                    b.status,
                    b.method
            });
        }
    }

    private void loadSelectedPatientHistory() {
        if (patientSelector == null) return;
        PatientRow selected = (PatientRow) patientSelector.getSelectedItem();
        if (selected == null) return;

        patientHistoryModel.setRowCount(0);
        for (HistoryRow h : MockClinicData.getHistoryForPatient(selected.id)) {
            patientHistoryModel.addRow(new Object[]{h.dateTime(), h.doctorName(), h.department(), h.symptoms(), h.diagnosis(), h.rxSummary()});
        }
    }
}
