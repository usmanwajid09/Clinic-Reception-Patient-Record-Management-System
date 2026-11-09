package com.clinic.ui.panels;

import com.clinic.model.*;
import com.clinic.service.IAppointmentService;
import com.clinic.service.IAuthService;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.dialogs.BookAppointmentDialog;
import com.clinic.ui.dialogs.RescheduleAppointmentDialog;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Appointment Scheduling, Queue Management, and Conflict Prevention Panel (FR11 - FR16).
 * Enforces role boundaries: Receptionists schedule & reschedule; Clinicians view their own queue.
 */
public class AppointmentManagementPanel extends JPanel {
    private final IAppointmentService appointmentService;
    private final IDoctorService doctorService;
    private final IPatientService patientService;
    private final IAuthService authService;

    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> doctorFilter;
    private JComboBox<String> statusFilter;
    private JTextField dateFilterField;

    private StatCard totalScheduledCard;
    private StatCard totalCompletedCard;
    private StatCard totalNoShowCard;

    private ModernButton bookBtn;
    private ModernButton rescheduleBtn;
    private ModernButton cancelBtn;
    private ModernButton noShowBtn;

    public AppointmentManagementPanel(IAppointmentService appointmentService,
                                      IDoctorService doctorService,
                                      IPatientService patientService,
                                      IAuthService authService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadAppointments();
        applyRolePermissions();
    }

    public AppointmentManagementPanel(IAppointmentService appointmentService,
                                      IDoctorService doctorService,
                                      IPatientService patientService) {
        this(appointmentService, doctorService, patientService, null);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 14));
        topPanel.setOpaque(false);

        // Header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Appointment Scheduling & Queue Management", IconFactory.getAppointmentsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Queue tracking (FR12), double-booking prevention engine (FR13), rescheduling (FR14), and status audit (FR15-16)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // KPI Stat Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        statsPanel.setOpaque(false);

        totalScheduledCard = new StatCard("Active Scheduled", "0", "Awaiting consultation", ThemeColors.INFO);
        totalCompletedCard = new StatCard("Consultations Completed", "0", "Finalized & billed", ThemeColors.SUCCESS);
        totalNoShowCard = new StatCard("No-Show / Cancelled", "0", "Unfulfilled appointments", ThemeColors.WARNING);

        statsPanel.add(totalScheduledCard);
        statsPanel.add(totalCompletedCard);
        statsPanel.add(totalNoShowCard);

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

        noShowBtn = ModernButton.secondary("Mark No-Show (FR15)");
        noShowBtn.addActionListener(e -> handleMarkNoShow());

        rescheduleBtn = ModernButton.secondary("Reschedule (FR14)");
        rescheduleBtn.addActionListener(e -> handleReschedule());

        cancelBtn = ModernButton.danger("Cancel (FR14)");
        cancelBtn.addActionListener(e -> handleCancel());

        bookBtn = new ModernButton("+ Book Appointment (FR11)");
        bookBtn.addActionListener(e -> {
            BookAppointmentDialog dialog = new BookAppointmentDialog(SwingUtilities.getWindowAncestor(this),
                    appointmentService, patientService, doctorService);
            dialog.setVisible(true);
            if (dialog.isBooked()) {
                loadAppointments();
                ToastNotification.showSuccess(this, "Appointment Booked", "Patient appointment successfully confirmed.");
            }
        });

        btnBox.add(noShowBtn);
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
        UIStyle.applyStatusBadge(appointmentTable, 7);

        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        appointmentTable.getColumnModel().getColumn(7).setPreferredWidth(105);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Enforces privacy and role capabilities.
     * Clinicians only see their own queue; Receptionists handle booking & rescheduling.
     */
    public void applyRolePermissions() {
        if (authService == null) return;
        User user = authService.getCurrentUser().orElse(null);
        if (user instanceof DoctorUser docUser) {
            // Clinician mode: Hide booking and cancellation
            bookBtn.setVisible(false);
            rescheduleBtn.setVisible(false);
            cancelBtn.setVisible(false);
            noShowBtn.setVisible(true);

            // Lock doctor filter to this doctor profile
            Doctor doc = doctorService.getDoctorById(docUser.getDoctorProfileId()).orElse(null);
            if (doc != null) {
                doctorFilter.removeAllItems();
                doctorFilter.addItem(doc.getName() + " (" + doc.getDoctorId() + ")");
                doctorFilter.setEnabled(false);
            }
        } else if (user != null && user.getRole() == Role.RECEPTIONIST) {
            bookBtn.setVisible(true);
            rescheduleBtn.setVisible(true);
            cancelBtn.setVisible(true);
            noShowBtn.setVisible(true);
            doctorFilter.setEnabled(true);
        } else if (user != null && user.getRole() == Role.ADMINISTRATOR) {
            bookBtn.setVisible(false);
            rescheduleBtn.setVisible(false);
            cancelBtn.setVisible(false);
            noShowBtn.setVisible(false);
            doctorFilter.setEnabled(true);
        }
    }

    public void loadAppointments() {
        applyRolePermissions();
        filterAppointments();
    }

    private void filterAppointments() {
        User user = authService != null ? authService.getCurrentUser().orElse(null) : null;
        String filterDoctorId = null;

        if (user instanceof DoctorUser docUser) {
            filterDoctorId = docUser.getDoctorProfileId();
        } else {
            String selectedDocStr = (String) doctorFilter.getSelectedItem();
            if (selectedDocStr != null && !selectedDocStr.equals("All Doctors")) {
                int openIdx = selectedDocStr.lastIndexOf('(');
                int closeIdx = selectedDocStr.lastIndexOf(')');
                if (openIdx >= 0 && closeIdx > openIdx) {
                    filterDoctorId = selectedDocStr.substring(openIdx + 1, closeIdx);
                }
            }
        }

        String selectedStatusStr = (String) statusFilter.getSelectedItem();
        AppointmentStatus filterStatus = null;
        if (selectedStatusStr != null && !selectedStatusStr.equals("All Statuses")) {
            try {
                filterStatus = AppointmentStatus.valueOf(selectedStatusStr.toUpperCase().replace("-", "_"));
            } catch (Exception ignored) {}
        }

        LocalDate filterDate = null;
        String dateText = dateFilterField.getText().trim();
        if (!dateText.isEmpty()) {
            try {
                filterDate = LocalDate.parse(dateText);
            } catch (Exception ignored) {}
        }

        final String finalDocId = filterDoctorId;
        final AppointmentStatus finalStatus = filterStatus;
        final LocalDate finalDate = filterDate;

        List<Appointment> list = appointmentService.getAllAppointments().stream()
                .filter(a -> finalDocId == null || a.getDoctorId().equalsIgnoreCase(finalDocId))
                .filter(a -> finalStatus == null || a.getStatus() == finalStatus)
                .filter(a -> finalDate == null || a.getAppointmentDate().equals(finalDate))
                .toList();
        renderAppointments(list);
    }

    private void renderAppointments(List<Appointment> list) {
        tableModel.setRowCount(0);
        int scheduledCount = 0;
        int completedCount = 0;
        int unfulfilledCount = 0;

        for (Appointment app : list) {
            Patient p = patientService.getPatientById(app.getPatientId()).orElse(null);
            Doctor d = doctorService.getDoctorById(app.getDoctorId()).orElse(null);
            Department dept = d != null ? d.getDepartment() : null;

            if (app.getStatus() == AppointmentStatus.SCHEDULED) scheduledCount++;
            else if (app.getStatus() == AppointmentStatus.COMPLETED) completedCount++;
            else unfulfilledCount++;

            tableModel.addRow(new Object[]{
                    "#" + app.getTokenNumber(),
                    app.getAppointmentId(),
                    app.getAppointmentDate().toString(),
                    app.getTimeSlot().getFormatted(),
                    p != null ? p.getName() : app.getPatientId(),
                    d != null ? d.getName() : app.getDoctorId(),
                    dept != null ? dept.getName() : "General",
                    app.getStatus().name(),
                    app.getNotes() != null ? app.getNotes() : ""
            });
        }

        totalScheduledCard.setValue(String.valueOf(scheduledCount));
        totalCompletedCard.setValue(String.valueOf(completedCount));
        totalNoShowCard.setValue(String.valueOf(unfulfilledCount));
    }

    private void handleReschedule() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select an appointment from the table.");
            return;
        }

        String apptId = (String) tableModel.getValueAt(row, 1);
        appointmentService.getAppointmentById(apptId).ifPresent(app -> {
            if (app.getStatus() != AppointmentStatus.SCHEDULED) {
                ToastNotification.showWarning(this, "Invalid Status", "Only active Scheduled appointments can be rescheduled.");
                return;
            }

            RescheduleAppointmentDialog dialog = new RescheduleAppointmentDialog(SwingUtilities.getWindowAncestor(this),
                    app, appointmentService, doctorService);
            dialog.setVisible(true);
            if (dialog.isRescheduled()) {
                loadAppointments();
                ToastNotification.showSuccess(this, "Rescheduled", "Appointment rescheduled successfully.");
            }
        });
    }

    private void handleCancel() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select an appointment to cancel.");
            return;
        }

        String apptId = (String) tableModel.getValueAt(row, 1);
        appointmentService.getAppointmentById(apptId).ifPresent(app -> {
            if (app.getStatus() != AppointmentStatus.SCHEDULED) {
                ToastNotification.showWarning(this, "Invalid Status", "Only active Scheduled appointments can be cancelled.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Cancel Appointment " + apptId + " for Token #" + app.getTokenNumber() + "?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                appointmentService.cancelAppointment(apptId, "Cancelled by front desk reception");
                loadAppointments();
                ToastNotification.showInfo(this, "Cancelled", "Appointment " + apptId + " marked as Cancelled.");
            }
        });
    }

    private void handleMarkNoShow() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select an appointment from the table.");
            return;
        }

        String apptId = (String) tableModel.getValueAt(row, 1);
        appointmentService.getAppointmentById(apptId).ifPresent(app -> {
            if (app.getStatus() != AppointmentStatus.SCHEDULED) {
                ToastNotification.showWarning(this, "Invalid Status", "Only active Scheduled appointments can be marked No-Show.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Mark Token #" + app.getTokenNumber() + " (" + apptId + ") as No-Show?",
                    "Confirm No-Show", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                appointmentService.updateAppointmentStatus(apptId, AppointmentStatus.NO_SHOW);
                loadAppointments();
                ToastNotification.showWarning(this, "No-Show Recorded", "Patient marked as No-Show for Token #" + app.getTokenNumber());
            }
        });
    }
}
