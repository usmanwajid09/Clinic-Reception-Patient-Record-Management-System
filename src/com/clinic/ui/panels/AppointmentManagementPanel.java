package com.clinic.ui.panels;

import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.dialogs.BookAppointmentDialog;
import com.clinic.ui.dialogs.RescheduleAppointmentDialog;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.AppointmentRow;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.mock.MockClinicData.UiRole;
import com.clinic.ui.mock.MockClinicData.UiUser;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/**
 * Presentation panel for Appointment Scheduling, Token Display, and Queue Management UI.
 */
public class AppointmentManagementPanel extends JPanel {
    private final UiUser currentUser;

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

    public AppointmentManagementPanel(UiUser currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        applyRoleVisibility();
        loadAppointments();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 14));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Appointment Scheduling & Queue Management", IconFactory.getAppointmentsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage daily appointment tokens, slot bookings, rescheduling, and status queues");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        statsPanel.setOpaque(false);

        totalScheduledCard = new StatCard("Active Scheduled", "0", "Awaiting consultation", ThemeColors.INFO);
        totalCompletedCard = new StatCard("Consultations Completed", "0", "Seen by clinician", ThemeColors.SUCCESS);
        totalNoShowCard = new StatCard("No-Show / Cancelled", "0", "Unfulfilled visits", ThemeColors.WARNING);

        statsPanel.add(totalScheduledCard);
        statsPanel.add(totalCompletedCard);
        statsPanel.add(totalNoShowCard);

        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setOpaque(false);

        JLabel docLabel = new JLabel("Doctor:");
        docLabel.setFont(UIStyle.FONT_BOLD);
        doctorFilter = new JComboBox<>();
        doctorFilter.setFont(UIStyle.FONT_REGULAR);
        doctorFilter.setBackground(Color.WHITE);
        doctorFilter.addItem("All Doctors");
        for (DoctorRow d : MockClinicData.getDoctors()) {
            doctorFilter.addItem(d.name + " (" + d.id + ")");
        }
        doctorFilter.addActionListener(e -> filterAppointments());

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(UIStyle.FONT_BOLD);
        statusFilter = new JComboBox<>(new String[]{"All Statuses", "SCHEDULED", "COMPLETED", "CANCELLED", "NO_SHOW"});
        statusFilter.setFont(UIStyle.FONT_REGULAR);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> filterAppointments());

        JLabel dateLabel = new JLabel("Date:");
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

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBox.setOpaque(false);

        noShowBtn = ModernButton.secondary("Mark No-Show");
        noShowBtn.addActionListener(e -> updateSelectedStatus("NO_SHOW"));

        rescheduleBtn = ModernButton.secondary("Reschedule");
        rescheduleBtn.addActionListener(e -> handleReschedule());

        cancelBtn = ModernButton.danger("Cancel");
        cancelBtn.addActionListener(e -> updateSelectedStatus("CANCELLED"));

        bookBtn = new ModernButton("+ Book Appointment");
        bookBtn.addActionListener(e -> {
            BookAppointmentDialog dialog = new BookAppointmentDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isBooked()) {
                loadAppointments();
                ToastNotification.showSuccess(this, "Appointment Booked", "New appointment row added to queue view.");
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

        String[] columns = {"Token #", "Appt ID", "Date", "Time Slot", "Patient Name", "Doctor", "Department", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        UIStyle.styleTable(appointmentTable);
        UIStyle.applyStatusBadge(appointmentTable, 7);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void applyRoleVisibility() {
        UiRole role = currentUser != null ? currentUser.role() : UiRole.RECEPTIONIST;
        if (role == UiRole.DOCTOR) {
            bookBtn.setVisible(false);
            rescheduleBtn.setVisible(false);
            cancelBtn.setVisible(false);
            noShowBtn.setVisible(true);

            String docId = currentUser.linkedDoctorId();
            if (docId != null) {
                MockClinicData.findDoctorById(docId).ifPresent(doc -> {
                    doctorFilter.removeAllItems();
                    doctorFilter.addItem(doc.name + " (" + doc.id + ")");
                    doctorFilter.setEnabled(false);
                });
            }
        } else if (role == UiRole.ADMINISTRATOR) {
            bookBtn.setVisible(false);
            rescheduleBtn.setVisible(false);
            cancelBtn.setVisible(false);
            noShowBtn.setVisible(false);
            doctorFilter.setEnabled(true);
        } else {
            bookBtn.setVisible(true);
            rescheduleBtn.setVisible(true);
            cancelBtn.setVisible(true);
            noShowBtn.setVisible(true);
            doctorFilter.setEnabled(true);
        }
    }

    public void loadAppointments() {
        filterAppointments();
    }

    private void filterAppointments() {
        String filterDoctorId = null;
        if (currentUser != null && currentUser.role() == UiRole.DOCTOR && currentUser.linkedDoctorId() != null) {
            filterDoctorId = currentUser.linkedDoctorId();
        } else {
            String selectedDoc = (String) doctorFilter.getSelectedItem();
            if (selectedDoc != null && !"All Doctors".equals(selectedDoc)) {
                int openIdx = selectedDoc.lastIndexOf('(');
                int closeIdx = selectedDoc.lastIndexOf(')');
                if (openIdx >= 0 && closeIdx > openIdx) {
                    filterDoctorId = selectedDoc.substring(openIdx + 1, closeIdx);
                }
            }
        }

        String selectedStatus = (String) statusFilter.getSelectedItem();
        String dateText = dateFilterField.getText().trim();

        tableModel.setRowCount(0);
        int scheduledCount = 0;
        int completedCount = 0;
        int unfulfilledCount = 0;

        for (AppointmentRow app : MockClinicData.getAppointments()) {
            if (filterDoctorId != null && !app.doctorId.equalsIgnoreCase(filterDoctorId)) continue;
            if (selectedStatus != null && !"All Statuses".equals(selectedStatus) && !app.status.equalsIgnoreCase(selectedStatus)) continue;
            if (!dateText.isEmpty() && !app.date.equals(dateText)) continue;

            if ("SCHEDULED".equalsIgnoreCase(app.status)) scheduledCount++;
            else if ("COMPLETED".equalsIgnoreCase(app.status)) completedCount++;
            else unfulfilledCount++;

            tableModel.addRow(new Object[]{
                    "#" + app.tokenNumber,
                    app.apptId,
                    app.date,
                    app.timeSlot,
                    app.patientName,
                    app.doctorName,
                    app.department,
                    app.status,
                    app.notes
            });
        }

        totalScheduledCard.setValue(String.valueOf(scheduledCount));
        totalCompletedCard.setValue(String.valueOf(completedCount));
        totalNoShowCard.setValue(String.valueOf(unfulfilledCount));
    }

    private AppointmentRow findSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select an appointment row from the table.");
            return null;
        }
        String apptId = (String) tableModel.getValueAt(row, 1);
        return MockClinicData.getAppointments().stream().filter(a -> a.apptId.equals(apptId)).findFirst().orElse(null);
    }

    private void handleReschedule() {
        AppointmentRow app = findSelectedAppointment();
        if (app == null) return;
        if (!"SCHEDULED".equalsIgnoreCase(app.status)) {
            ToastNotification.showWarning(this, "Invalid Selection", "Only SCHEDULED appointments can be rescheduled.");
            return;
        }
        RescheduleAppointmentDialog dialog = new RescheduleAppointmentDialog(SwingUtilities.getWindowAncestor(this), app);
        dialog.setVisible(true);
        if (dialog.isRescheduled()) {
            loadAppointments();
            ToastNotification.showSuccess(this, "Rescheduled", "Appointment " + app.apptId + " updated.");
        }
    }

    private void updateSelectedStatus(String newStatus) {
        AppointmentRow app = findSelectedAppointment();
        if (app == null) return;
        if (!"SCHEDULED".equalsIgnoreCase(app.status)) {
            ToastNotification.showWarning(this, "Invalid Selection", "Only SCHEDULED appointments can be updated.");
            return;
        }
        app.status = newStatus;
        loadAppointments();
        ToastNotification.showInfo(this, "Status Updated", "Appointment " + app.apptId + " marked as " + newStatus + ".");
    }
}
