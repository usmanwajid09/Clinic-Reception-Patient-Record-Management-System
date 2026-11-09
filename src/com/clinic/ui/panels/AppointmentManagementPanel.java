package com.clinic.ui.panels;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.service.IAppointmentService;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.dialogs.BookAppointmentDialog;
import com.clinic.ui.dialogs.RescheduleAppointmentDialog;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Presentation panel for Appointment Scheduling and Queue Management (FR11 - FR16).
 * Enforces strict double-booking collision prevention (FR13),
 * displays doctor's appointment list for selected date (FR16),
 * and handles rescheduling/cancellations (FR14).
 */
public class AppointmentManagementPanel extends JPanel {
    private final IAppointmentService appointmentService;
    private final IDoctorService doctorService;
    private final IPatientService patientService;

    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> doctorFilter;
    private JComboBox<String> statusFilter;
    private JTextField dateFilterField;

    private StatCard totalCard;
    private StatCard scheduledCard;
    private StatCard completedCard;

    public AppointmentManagementPanel(IAppointmentService appointmentService,
                                      IDoctorService doctorService,
                                      IPatientService patientService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Appointment Scheduling & Queue Management");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Book consultations, enforce conflict prevention (FR13), track status (FR15), and review queues (FR16)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // KPI Summary Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        statsPanel.setOpaque(false);

        totalCard = new StatCard("Total Encounters", "0", "Across all clinicians", ThemeColors.INFO);
        scheduledCard = new StatCard("Active Queue (FR15)", "0", "Awaiting consultation", ThemeColors.WARNING);
        completedCard = new StatCard("Completed Visits (FR15)", "0", "Successfully finished", ThemeColors.SUCCESS);

        statsPanel.add(totalCard);
        statsPanel.add(scheduledCard);
        statsPanel.add(completedCard);

        // Toolbar: Filters & Actions
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setOpaque(false);

        JLabel docLabel = new JLabel("Doctor (FR16):");
        docLabel.setFont(UIStyle.FONT_BOLD);
        doctorFilter = new JComboBox<>();
        doctorFilter.setFont(UIStyle.FONT_REGULAR);
        doctorFilter.setBackground(Color.WHITE);
        doctorFilter.addItem("All Doctors");
        for (Doctor d : doctorService.getAllDoctors()) {
            doctorFilter.addItem(d.getName() + " (" + d.getDoctorId() + ")");
        }
        doctorFilter.addActionListener(e -> filterAppointments());

        JLabel statusLabel = new JLabel("Status (FR15):");
        statusLabel.setFont(UIStyle.FONT_BOLD);
        statusFilter = new JComboBox<>(new String[]{"All Statuses", "Scheduled", "Completed", "Cancelled", "No-Show"});
        statusFilter.setFont(UIStyle.FONT_REGULAR);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> filterAppointments());

        JLabel dateLabel = new JLabel("Date (FR16):");
        dateLabel.setFont(UIStyle.FONT_BOLD);
        dateFilterField = UIStyle.createStyledTextField(10);
        dateFilterField.setText(LocalDate.now().toString());

        ModernButton applyDateBtn = ModernButton.secondary("Filter Date");
        applyDateBtn.addActionListener(e -> filterAppointments());

        ModernButton clearDateBtn = ModernButton.secondary("Show All");
        clearDateBtn.addActionListener(e -> {
            dateFilterField.setText("");
            filterAppointments();
        });

        filterBox.add(docLabel);
        filterBox.add(doctorFilter);
        filterBox.add(statusLabel);
        filterBox.add(statusFilter);
        filterBox.add(dateLabel);
        filterBox.add(dateFilterField);
        filterBox.add(applyDateBtn);
        filterBox.add(clearDateBtn);

        // Actions
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBox.setOpaque(false);

        ModernButton bookBtn = new ModernButton("+ Book Appointment (FR11)");
        bookBtn.addActionListener(e -> {
            BookAppointmentDialog dialog = new BookAppointmentDialog(SwingUtilities.getWindowAncestor(this),
                    appointmentService, patientService, doctorService);
            dialog.setVisible(true);
            if (dialog.isBooked()) {
                loadAppointments();
            }
        });

        ModernButton rescheduleBtn = ModernButton.secondary("Reschedule (FR14)");
        rescheduleBtn.addActionListener(e -> handleReschedule());

        ModernButton cancelBtn = ModernButton.danger("Cancel (FR14)");
        cancelBtn.addActionListener(e -> handleCancel());

        btnBox.add(rescheduleBtn);
        btnBox.add(cancelBtn);
        btnBox.add(bookBtn);

        toolbar.add(filterBox, BorderLayout.WEST);
        toolbar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Token #", "Appt ID", "Date", "Time Slot", "Patient Name", "Doctor", "Department", "Status (FR15)", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        UIStyle.styleTable(appointmentTable);
        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        appointmentTable.getColumnModel().getColumn(7).setPreferredWidth(95);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadAppointments() {
        filterAppointments();
    }

    private void filterAppointments() {
        tableModel.setRowCount(0);
        List<Appointment> all = appointmentService.getAllAppointments();

        String selectedDocStr = (String) doctorFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String dateStr = dateFilterField.getText().trim();

        int scheduled = 0;
        int completed = 0;

        for (Appointment app : all) {
            if (app.getStatus() == AppointmentStatus.SCHEDULED) scheduled++;
            if (app.getStatus() == AppointmentStatus.COMPLETED) completed++;

            // Doctor filter
            if (selectedDocStr != null && !selectedDocStr.equals("All Doctors")) {
                if (!selectedDocStr.contains(app.getDoctorId())) {
                    continue;
                }
            }

            // Status filter
            if (selectedStatus != null && !selectedStatus.equals("All Statuses")) {
                if (!app.getStatus().name().equalsIgnoreCase(selectedStatus)) {
                    continue;
                }
            }

            // Date filter
            if (!dateStr.isEmpty()) {
                if (!app.getAppointmentDate().toString().equalsIgnoreCase(dateStr)) {
                    continue;
                }
            }

            Patient patient = patientService.getPatientById(app.getPatientId()).orElse(null);
            Doctor doctor = doctorService.getDoctorById(app.getDoctorId()).orElse(null);

            tableModel.addRow(new Object[]{
                    "#" + app.getTokenNumber(),
                    app.getAppointmentId(),
                    DateTimeUtil.formatDate(app.getAppointmentDate()),
                    app.getTimeSlot().getFormatted(),
                    patient != null ? patient.getName() : app.getPatientId(),
                    doctor != null ? doctor.getName() : app.getDoctorId(),
                    doctor != null ? doctor.getDepartment().getName() : "N/A",
                    app.getStatus().getDisplayName(),
                    app.getNotes()
            });
        }

        totalCard.setValue(String.valueOf(all.size()));
        scheduledCard.setValue(String.valueOf(scheduled));
        completedCard.setValue(String.valueOf(completed));
    }

    private void handleReschedule() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table to reschedule.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String apptId = (String) tableModel.getValueAt(row, 1);
        appointmentService.getAppointmentById(apptId).ifPresent(app -> {
            if (app.getStatus() != AppointmentStatus.SCHEDULED) {
                JOptionPane.showMessageDialog(this, "Only Scheduled appointments can be rescheduled.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }
            RescheduleAppointmentDialog dialog = new RescheduleAppointmentDialog(SwingUtilities.getWindowAncestor(this), app, appointmentService, doctorService);
            dialog.setVisible(true);
            if (dialog.isRescheduled()) {
                loadAppointments();
            }
        });
    }

    private void handleCancel() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table to cancel.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String apptId = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel appointment #" + apptId + "? (FR14)",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String reason = JOptionPane.showInputDialog(this, "Enter reason for cancellation (optional):", "Cancellation Reason", JOptionPane.PLAIN_MESSAGE);
            appointmentService.cancelAppointment(apptId, reason);
            loadAppointments();
        }
    }
}
