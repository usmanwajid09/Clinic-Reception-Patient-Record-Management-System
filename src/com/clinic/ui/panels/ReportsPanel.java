package com.clinic.ui.panels;

import com.clinic.config.AppConfig;
import com.clinic.model.Appointment;
import com.clinic.model.Consultation;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.service.IReportService;
import com.clinic.service.IReportService.*;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Presentation panel for Clinical, Operational, and Financial Reports (FR27, FR28, FR29, FR30).
 */
public class ReportsPanel extends JPanel {
    private final IReportService reportService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;

    private JTextField startDateField;
    private JTextField endDateField;

    // Sub-tab tables
    private DefaultTableModel appointmentModel;
    private DefaultTableModel doctorModel;
    private DefaultTableModel patientHistoryModel;
    private DefaultTableModel revenueModel;

    private JComboBox<Patient> patientSelector;
    private JLabel summaryTextLabel;

    public ReportsPanel(IReportService reportService,
                        IPatientService patientService,
                        IDoctorService doctorService) {
        this.reportService = reportService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        runReports();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Clinical Analytics & Executive Reporting");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Appointment stats (FR27), doctor productivity (FR28), patient visit logs (FR29), and revenue audits (FR30)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Filter Bar (Date range)
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

        ModernButton runBtn = new ModernButton("Generate Reports");
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

        ModernButton monthBtn = ModernButton.secondary("This Month");
        monthBtn.addActionListener(e -> {
            startDateField.setText(LocalDate.now().withDayOfMonth(1).toString());
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
        filterBar.add(monthBtn);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(filterBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Tabbed Pane for the 4 Mandatory Reports
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_BOLD);

        tabbedPane.addTab("Appointment Reports (FR27)", createAppointmentReportTab());
        tabbedPane.addTab("Doctor Performance (FR28)", createDoctorReportTab());
        tabbedPane.addTab("Patient Visit History (FR29)", createPatientHistoryTab());
        tabbedPane.addTab("Revenue & Billing (FR30)", createRevenueReportTab());

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

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDoctorReportTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Doctor ID", "Doctor Name", "Department", "Specialization", "Total Consultations", "Unique Patients Seen", "Total Revenue Share"};
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
        JLabel pLabel = new JLabel("Select Patient (FR29):");
        pLabel.setFont(UIStyle.FONT_BOLD);

        List<Patient> patients = patientService.getAllPatients();
        patientSelector = new JComboBox<>(patients.toArray(new Patient[0]));
        patientSelector.setFont(UIStyle.FONT_REGULAR);
        patientSelector.setBackground(Color.WHITE);
        patientSelector.addActionListener(e -> loadSelectedPatientHistory());

        selectBar.add(pLabel);
        selectBar.add(patientSelector);
        panel.add(selectBar, BorderLayout.NORTH);

        String[] cols = {"Consultation Date", "Attending Doctor", "Department", "Symptoms", "Diagnosis", "Notes", "Prescription Summary"};
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

        String[] cols = {"Invoice ID", "Date", "Consultation Ref", "Doctor", "Doctor Fee", "Clinic Fee", "Total", "Status", "Payment Method"};
        revenueModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(revenueModel);
        UIStyle.styleTable(table);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void runReports() {
        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            end = LocalDate.parse(endDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Appointment Report (FR27)
        AppointmentReportData apptData = reportService.getAppointmentReport(start, end);
        appointmentModel.setRowCount(0);
        for (Appointment a : apptData.appointments()) {
            Patient p = patientService.getPatientById(a.getPatientId()).orElse(null);
            Doctor d = doctorService.getDoctorById(a.getDoctorId()).orElse(null);
            appointmentModel.addRow(new Object[]{
                    a.getAppointmentId(),
                    DateTimeUtil.formatDate(a.getAppointmentDate()),
                    a.getTimeSlot().getFormatted(),
                    p != null ? p.getName() : a.getPatientId(),
                    d != null ? d.getName() : a.getDoctorId(),
                    d != null ? d.getDepartment().getName() : "N/A",
                    a.getStatus().getDisplayName(),
                    a.getNotes()
            });
        }

        // 2. Doctor Performance Report (FR28)
        List<DoctorReportData> docReports = reportService.getDoctorPerformanceReports(start, end);
        doctorModel.setRowCount(0);
        for (DoctorReportData dr : docReports) {
            doctorModel.addRow(new Object[]{
                    dr.doctorId(),
                    dr.doctorName(),
                    dr.department(),
                    dr.specialization(),
                    dr.totalConsultations(),
                    dr.uniquePatientsSeen(),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", dr.totalRevenueGenerated())
            });
        }

        // 3. Patient History Report (FR29)
        loadSelectedPatientHistory();

        // 4. Revenue Report (FR30)
        RevenueReportData revData = reportService.getRevenueReport(start, end);
        revenueModel.setRowCount(0);
        for (com.clinic.model.Bill b : revData.bills()) {
            Doctor d = doctorService.getDoctorById(b.getDoctorId()).orElse(null);
            revenueModel.addRow(new Object[]{
                    b.getBillId(),
                    DateTimeUtil.formatDateTime(b.getBilledAt()),
                    b.getConsultationId(),
                    d != null ? d.getName() : b.getDoctorId(),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", b.getDoctorConsultationFee()),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", b.getClinicServiceCharge()),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", b.getTotalAmount()),
                    b.getPaymentStatus().getDisplayName(),
                    b.getPaymentMethod() != null ? b.getPaymentMethod().getDisplayName() : "Pending"
            });
        }
    }

    private void loadSelectedPatientHistory() {
        if (patientSelector == null) return;
        Patient selected = (Patient) patientSelector.getSelectedItem();
        if (selected == null) return;

        PatientHistoryReportData hist = reportService.getPatientVisitHistoryReport(selected.getPatientId());
        patientHistoryModel.setRowCount(0);
        for (Consultation c : hist.consultationHistory()) {
            Doctor doc = doctorService.getDoctorById(c.getDoctorId()).orElse(null);
            String rxStr = (c.getPrescription() != null && !c.getPrescription().isEmpty())
                    ? c.getPrescription().getItems().size() + " medicines" : "No Rx";

            patientHistoryModel.addRow(new Object[]{
                    DateTimeUtil.formatDateTime(c.getConsultationTime()),
                    doc != null ? doc.getName() : c.getDoctorId(),
                    doc != null ? doc.getDepartment().getName() : "N/A",
                    c.getSymptoms(),
                    c.getDiagnosis(),
                    c.getNotes(),
                    rxStr
            });
        }
    }
}

