package com.clinic.ui.panels;

import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.dialogs.DoctorAvailabilityDialog;
import com.clinic.ui.dialogs.PatientHistoryDialog;
import com.clinic.ui.dialogs.PrescriptionItemDialog;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.*;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/**
 * Presentation panel for Doctor Clinical Workstation and Electronic Prescription Editor UI.
 */
public class DoctorConsultationPanel extends JPanel {
    private final UiUser currentUser;

    private String currentDoctorId = "DOC-101";
    private AppointmentRow activeAppointment;
    private PatientRow activePatient;
    private ConsultationMock activeConsultation;

    private JLabel doctorSessionBadge;
    private ModernButton dutyStatusBtn;
    private DefaultListModel<AppointmentRow> queueListModel;
    private JList<AppointmentRow> queueList;

    private JLabel patientBannerLabel;
    private ModernButton viewHistoryBtn;
    private JTextArea symptomsArea;
    private JTextField diagnosisField;
    private JTextArea notesArea;

    private JTable rxTable;
    private DefaultTableModel rxTableModel;
    private ModernButton addRxBtn;
    private ModernButton removeRxBtn;
    private ModernButton finalizeBtn;
    private JLabel lockStatusBadge;

    public DoctorConsultationPanel(UiUser currentUser) {
        this.currentUser = currentUser;
        if (currentUser != null && currentUser.linkedDoctorId() != null) {
            this.currentDoctorId = currentUser.linkedDoctorId();
        }

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadDoctorQueue();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(16, 0));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Doctor Clinical Workstation & Consultation Station", IconFactory.getWorkstationIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Inspect daily queue, examine patient history, enter diagnosis notes, and compose prescriptions");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel doctorContextPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        doctorContextPanel.setOpaque(false);

        doctorSessionBadge = new JLabel();
        doctorSessionBadge.setIcon(IconFactory.getDoctorsIcon(24));
        doctorSessionBadge.setIconTextGap(8);
        doctorSessionBadge.setFont(UIStyle.FONT_BOLD);
        doctorSessionBadge.setForeground(ThemeColors.PRIMARY_DARK);

        DoctorRow doc = MockClinicData.findDoctorById(currentDoctorId).orElse(null);
        String docDisplay = doc != null ? doc.name + " (" + doc.specialization + ")" : (currentUser != null ? currentUser.fullName() : "Doctor Session");
        doctorSessionBadge.setText("Clinician: " + docDisplay);

        dutyStatusBtn = ModernButton.secondary("Duty Availability");
        dutyStatusBtn.addActionListener(e -> handleToggleOwnAvailability());
        refreshDutyButtonLabel();

        doctorContextPanel.add(doctorSessionBadge);
        doctorContextPanel.add(dutyStatusBtn);

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(doctorContextPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Split Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(340);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Left: Patient Queue
        JPanel leftQueueCard = UIStyle.createCard();
        leftQueueCard.setLayout(new BorderLayout(0, 10));

        JLabel queueTitle = new JLabel("Today's Patient Queue:");
        queueTitle.setFont(UIStyle.FONT_HEADER);
        queueTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        leftQueueCard.add(queueTitle, BorderLayout.NORTH);

        queueListModel = new DefaultListModel<>();
        queueList = new JList<>(queueListModel);
        queueList.setFont(UIStyle.FONT_REGULAR);
        queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queueList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AppointmentRow app) {
                    setText(app.toString());
                    setBorder(new EmptyBorder(8, 8, 8, 8));
                    if ("COMPLETED".equalsIgnoreCase(app.status)) {
                        setForeground(ThemeColors.SUCCESS);
                    } else if ("CANCELLED".equalsIgnoreCase(app.status)) {
                        setForeground(ThemeColors.DANGER);
                    } else {
                        setForeground(ThemeColors.TEXT_PRIMARY);
                    }
                }
                return this;
            }
        });

        queueList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                AppointmentRow selected = queueList.getSelectedValue();
                if (selected != null) {
                    loadAppointmentForConsultation(selected);
                }
            }
        });

        JScrollPane queueScroll = new JScrollPane(queueList);
        queueScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        leftQueueCard.add(queueScroll, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftQueueCard);

        // Right: Examination & Rx Form
        JPanel rightWorkstation = new JPanel(new BorderLayout(0, 12));
        rightWorkstation.setOpaque(false);

        JPanel patientCard = UIStyle.createCard();
        patientCard.setLayout(new BorderLayout(12, 0));

        patientBannerLabel = new JLabel("Select a patient from the queue to open clinical record.");
        patientBannerLabel.setFont(UIStyle.FONT_HEADER);
        patientBannerLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        viewHistoryBtn = ModernButton.secondary("View Visit History");
        viewHistoryBtn.setEnabled(false);
        viewHistoryBtn.addActionListener(e -> {
            if (activePatient != null) {
                new PatientHistoryDialog(SwingUtilities.getWindowAncestor(this), activePatient).setVisible(true);
            }
        });

        lockStatusBadge = new JLabel(" ");
        lockStatusBadge.setFont(UIStyle.FONT_BOLD);

        JPanel bannerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bannerActions.setOpaque(false);
        bannerActions.add(lockStatusBadge);
        bannerActions.add(viewHistoryBtn);

        patientCard.add(patientBannerLabel, BorderLayout.CENTER);
        patientCard.add(bannerActions, BorderLayout.EAST);
        rightWorkstation.add(patientCard, BorderLayout.NORTH);

        JPanel formCard = UIStyle.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel findingsHeader = new JLabel("Clinical Examination & Diagnosis");
        findingsHeader.setFont(UIStyle.FONT_HEADER);
        findingsHeader.setForeground(ThemeColors.PRIMARY_DARK);
        formCard.add(findingsHeader);
        formCard.add(Box.createVerticalStrut(8));

        JPanel findingsGrid = new JPanel(new GridLayout(3, 1, 6, 6));
        findingsGrid.setOpaque(false);

        symptomsArea = new JTextArea(2, 30);
        symptomsArea.setFont(UIStyle.FONT_REGULAR);
        symptomsArea.setBorder(UIStyle.createInputBorder());
        symptomsArea.setLineWrap(true);
        JPanel sympPanel = new JPanel(new BorderLayout());
        sympPanel.setOpaque(false);
        sympPanel.add(new JLabel("Patient Symptoms:"), BorderLayout.NORTH);
        sympPanel.add(new JScrollPane(symptomsArea), BorderLayout.CENTER);

        diagnosisField = UIStyle.createStyledTextField(24);
        JPanel diagPanel = new JPanel(new BorderLayout());
        diagPanel.setOpaque(false);
        diagPanel.add(new JLabel("Clinical Diagnosis *:"), BorderLayout.NORTH);
        diagPanel.add(diagnosisField, BorderLayout.CENTER);

        notesArea = new JTextArea(2, 30);
        notesArea.setFont(UIStyle.FONT_REGULAR);
        notesArea.setBorder(UIStyle.createInputBorder());
        notesArea.setLineWrap(true);
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setOpaque(false);
        notesPanel.add(new JLabel("Consultation & Lifestyle Notes:"), BorderLayout.NORTH);
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        findingsGrid.add(sympPanel);
        findingsGrid.add(diagPanel);
        findingsGrid.add(notesPanel);
        formCard.add(findingsGrid);
        formCard.add(Box.createVerticalStrut(14));

        JPanel rxHeaderPanel = new JPanel(new BorderLayout());
        rxHeaderPanel.setOpaque(false);
        JLabel rxHeader = new JLabel("Electronic Prescription");
        rxHeader.setFont(UIStyle.FONT_HEADER);
        rxHeader.setForeground(ThemeColors.PRIMARY_DARK);

        JPanel rxButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rxButtons.setOpaque(false);

        addRxBtn = new ModernButton("+ Add Medicine");
        addRxBtn.setEnabled(false);
        addRxBtn.addActionListener(e -> {
            if (activeConsultation == null) return;
            PrescriptionItemDialog dlg = new PrescriptionItemDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            RxItemRow item = dlg.getCreatedItem();
            if (item != null) {
                activeConsultation.items.add(item);
                updateRxTable();
                ToastNotification.showInfo(this, "Medicine Added", item.medicine + " added to prescription.");
            }
        });

        removeRxBtn = ModernButton.danger("Remove");
        removeRxBtn.setEnabled(false);
        removeRxBtn.addActionListener(e -> {
            int row = rxTable.getSelectedRow();
            if (activeConsultation != null && row >= 0 && row < activeConsultation.items.size()) {
                activeConsultation.items.remove(row);
                updateRxTable();
            }
        });

        rxButtons.add(removeRxBtn);
        rxButtons.add(addRxBtn);

        rxHeaderPanel.add(rxHeader, BorderLayout.WEST);
        rxHeaderPanel.add(rxButtons, BorderLayout.EAST);
        formCard.add(rxHeaderPanel);
        formCard.add(Box.createVerticalStrut(6));

        String[] rxCols = {"Medicine Name", "Dosage", "Frequency", "Duration", "Instructions"};
        rxTableModel = new DefaultTableModel(rxCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rxTable = new JTable(rxTableModel);
        UIStyle.styleTable(rxTable);
        JScrollPane rxScroll = new JScrollPane(rxTable);
        rxScroll.setPreferredSize(new Dimension(rxScroll.getPreferredSize().width, 140));
        rxScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        rxScroll.getViewport().setBackground(Color.WHITE);
        formCard.add(rxScroll);

        rightWorkstation.add(formCard, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        finalizeBtn = ModernButton.success("✓ Finalize & Lock Consultation");
        finalizeBtn.setEnabled(false);
        finalizeBtn.addActionListener(e -> handleFinalizeConsultation());

        footer.add(finalizeBtn);
        rightWorkstation.add(footer, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightWorkstation);
        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshDutyButtonLabel() {
        if (dutyStatusBtn == null) return;
        DoctorRow doc = MockClinicData.findDoctorById(currentDoctorId).orElse(null);
        String status = doc != null ? doc.status : "AVAILABLE";
        dutyStatusBtn.setText("Duty: " + status);
    }

    private void handleToggleOwnAvailability() {
        DoctorRow doc = MockClinicData.findDoctorById(currentDoctorId).orElse(null);
        if (doc == null) return;
        DoctorAvailabilityDialog dialog = new DoctorAvailabilityDialog(SwingUtilities.getWindowAncestor(this), doc);
        dialog.setVisible(true);
        if (dialog.isUpdated()) {
            refreshDutyButtonLabel();
            ToastNotification.showSuccess(this, "Availability Updated", doc.name + " marked as " + doc.status + ".");
        }
    }

    public void loadDoctorQueue() {
        refreshDutyButtonLabel();
        queueListModel.clear();
        String today = LocalDate.now().toString();
        for (AppointmentRow app : MockClinicData.getAppointments()) {
            if (app.doctorId.equalsIgnoreCase(currentDoctorId) && app.date.equals(today)) {
                queueListModel.addElement(app);
            }
        }
        resetConsultationEditor();
    }

    private void loadAppointmentForConsultation(AppointmentRow app) {
        this.activeAppointment = app;
        this.activePatient = MockClinicData.findPatientById(app.patientId).orElse(null);

        if (activePatient != null) {
            patientBannerLabel.setText(activePatient.name + " (" + activePatient.gender + " | DOB: " + activePatient.dob + ") — " + activePatient.phone);
            viewHistoryBtn.setEnabled(true);
        }

        this.activeConsultation = MockClinicData.getOrCreateMockConsultation(app.apptId);
        symptomsArea.setText(activeConsultation.symptoms);
        diagnosisField.setText(activeConsultation.diagnosis);
        notesArea.setText(activeConsultation.notes);

        updateRxTable();
        applyLockState(activeConsultation.locked);
    }

    private void applyLockState(boolean locked) {
        symptomsArea.setEditable(!locked);
        diagnosisField.setEditable(!locked);
        notesArea.setEditable(!locked);
        addRxBtn.setEnabled(!locked);
        removeRxBtn.setEnabled(!locked);
        finalizeBtn.setEnabled(!locked);

        if (activeConsultation == null) {
            lockStatusBadge.setText(" ");
        } else if (locked) {
            lockStatusBadge.setText("LOCKED");
            lockStatusBadge.setForeground(ThemeColors.SUCCESS);
        } else {
            lockStatusBadge.setText("IN-PROGRESS");
            lockStatusBadge.setForeground(ThemeColors.WARNING);
        }
    }

    private void updateRxTable() {
        rxTableModel.setRowCount(0);
        if (activeConsultation == null) return;
        for (RxItemRow item : activeConsultation.items) {
            rxTableModel.addRow(new Object[]{item.medicine, item.dosage, item.frequency, item.duration, item.instructions});
        }
    }

    private void resetConsultationEditor() {
        activeAppointment = null;
        activePatient = null;
        activeConsultation = null;
        rxTableModel.setRowCount(0);

        patientBannerLabel.setText("Select a patient from the queue to open clinical record.");
        viewHistoryBtn.setEnabled(false);
        symptomsArea.setText("");
        diagnosisField.setText("");
        notesArea.setText("");
        applyLockState(true);
    }

    private void handleFinalizeConsultation() {
        if (activeConsultation == null || activeAppointment == null) return;

        String diagnosis = diagnosisField.getText().trim();
        if (diagnosis.isEmpty()) {
            ToastNotification.showWarning(this, "Diagnosis Missing", "Please enter a clinical diagnosis.");
            diagnosisField.requestFocus();
            return;
        }

        activeConsultation.symptoms = symptomsArea.getText().trim();
        activeConsultation.diagnosis = diagnosis;
        activeConsultation.notes = notesArea.getText().trim();
        activeConsultation.locked = true;
        activeAppointment.status = "COMPLETED";

        applyLockState(true);
        queueList.repaint();
        ToastNotification.showSuccess(this, "Consultation Finalized", "Clinical view locked for " + activeAppointment.patientName + ".");
    }
}
