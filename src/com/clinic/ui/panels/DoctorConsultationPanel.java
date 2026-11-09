package com.clinic.ui.panels;

import com.clinic.model.*;
import com.clinic.service.*;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.dialogs.PatientHistoryDialog;
import com.clinic.ui.dialogs.PrescriptionItemDialog;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Presentation panel for Doctor Clinical Workstation & Electronic Prescriptions (FR17 - FR23).
 * Enables clinicians to open scheduled patient files (FR17), record findings (FR18),
 * prescribe medications (FR19 - FR21), and lock completed consultations (FR23).
 */
public class DoctorConsultationPanel extends JPanel {
    private final IConsultationService consultationService;
    private final IAppointmentService appointmentService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    private final IAuthService authService;

    // Active Consultation State
    private Appointment activeAppointment;
    private Patient activePatient;
    private Consultation activeConsultation;
    private final List<PrescriptionItem> prescriptionItems = new ArrayList<>();

    // UI Elements
    private JComboBox<Doctor> doctorSelector;
    private DefaultListModel<Appointment> queueListModel;
    private JList<Appointment> queueList;

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

    public DoctorConsultationPanel(IConsultationService consultationService,
                                   IAppointmentService appointmentService,
                                   IPatientService patientService,
                                   IDoctorService doctorService,
                                   IAuthService authService) {
        this.consultationService = consultationService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadDoctorQueue();
    }

    private void initComponents() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(16, 0));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Doctor Clinical Workstation & Consultation Station");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Review scheduled queue (FR16), examine patients (FR17), record diagnoses (FR18), and issue prescriptions (FR19-21)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel doctorPickerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        doctorPickerPanel.setOpaque(false);
        JLabel docLabel = new JLabel("Clinician Session:");
        docLabel.setFont(UIStyle.FONT_BOLD);
        List<Doctor> doctors = doctorService.getAllDoctors();
        doctorSelector = new JComboBox<>(doctors.toArray(new Doctor[0]));
        doctorSelector.setFont(UIStyle.FONT_REGULAR);
        doctorSelector.setBackground(Color.WHITE);
        doctorSelector.addActionListener(e -> loadDoctorQueue());

        doctorPickerPanel.add(docLabel);
        doctorPickerPanel.add(doctorSelector);

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(doctorPickerPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main Workspace Split (Left: Queue, Right: Clinical Record Editor)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(320);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Left Panel: Queue (FR16, FR17)
        JPanel leftQueueCard = UIStyle.createCard();
        leftQueueCard.setLayout(new BorderLayout(0, 10));

        JLabel queueTitle = new JLabel("Today's Patient Queue (FR16):");
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
                if (value instanceof Appointment app) {
                    Patient p = patientService.getPatientById(app.getPatientId()).orElse(null);
                    String name = p != null ? p.getName() : app.getPatientId();
                    setText("Token #" + app.getTokenNumber() + " - " + name + " (" + app.getTimeSlot().getFormatted() + ") [" + app.getStatus() + "]");
                    setBorder(new EmptyBorder(8, 8, 8, 8));
                    if (app.getStatus() == AppointmentStatus.COMPLETED) {
                        setForeground(ThemeColors.SUCCESS);
                    } else if (app.getStatus() == AppointmentStatus.CANCELLED) {
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
                Appointment selected = queueList.getSelectedValue();
                if (selected != null) {
                    loadAppointmentForConsultation(selected);
                }
            }
        });

        JScrollPane queueScroll = new JScrollPane(queueList);
        queueScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        leftQueueCard.add(queueScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftQueueCard);

        // Right Panel: Clinical Record & Prescription Editor (FR17 - FR23)
        JPanel rightWorkstation = new JPanel(new BorderLayout(0, 12));
        rightWorkstation.setOpaque(false);

        // Patient Banner
        JPanel patientCard = UIStyle.createCard();
        patientCard.setLayout(new BorderLayout(12, 0));

        patientBannerLabel = new JLabel("Select an appointment from the queue to start consultation (FR17).");
        patientBannerLabel.setFont(UIStyle.FONT_HEADER);
        patientBannerLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        viewHistoryBtn = ModernButton.secondary("View Medical History (FR4)");
        viewHistoryBtn.setEnabled(false);
        viewHistoryBtn.addActionListener(e -> {
            if (activePatient != null) {
                new PatientHistoryDialog(SwingUtilities.getWindowAncestor(this), activePatient, patientService, doctorService).setVisible(true);
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

        // Consultation Forms (Symptoms, Diagnosis, Notes, Rx)
        JPanel formCard = UIStyle.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        // Clinical Findings Section (FR18)
        JLabel findingsHeader = new JLabel("Clinical Examination & Diagnosis (FR18):");
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
        sympPanel.add(new JLabel("Symptoms:"), BorderLayout.NORTH);
        sympPanel.add(new JScrollPane(symptomsArea), BorderLayout.CENTER);

        diagnosisField = UIStyle.createStyledTextField(24);
        JPanel diagPanel = new JPanel(new BorderLayout());
        diagPanel.setOpaque(false);
        diagPanel.add(new JLabel("Clinical Diagnosis * (FR18):"), BorderLayout.NORTH);
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

        // Electronic Prescription Section (FR19, FR20, FR21)
        JPanel rxHeaderPanel = new JPanel(new BorderLayout());
        rxHeaderPanel.setOpaque(false);
        JLabel rxHeader = new JLabel("Electronic Prescription (FR19 - FR21):");
        rxHeader.setFont(UIStyle.FONT_HEADER);
        rxHeader.setForeground(ThemeColors.PRIMARY_DARK);

        JPanel rxButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rxButtons.setOpaque(false);

        addRxBtn = new ModernButton("+ Add Medicine (FR20)");
        addRxBtn.setEnabled(false);
        addRxBtn.addActionListener(e -> {
            PrescriptionItemDialog dlg = new PrescriptionItemDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            PrescriptionItem item = dlg.getCreatedItem();
            if (item != null) {
                prescriptionItems.add(item);
                updateRxTable();
            }
        });

        removeRxBtn = ModernButton.danger("Remove");
        removeRxBtn.setEnabled(false);
        removeRxBtn.addActionListener(e -> {
            int row = rxTable.getSelectedRow();
            if (row >= 0 && row < prescriptionItems.size()) {
                prescriptionItems.remove(row);
                updateRxTable();
            }
        });

        rxButtons.add(removeRxBtn);
        rxButtons.add(addRxBtn);

        rxHeaderPanel.add(rxHeader, BorderLayout.WEST);
        rxHeaderPanel.add(rxButtons, BorderLayout.EAST);
        formCard.add(rxHeaderPanel);
        formCard.add(Box.createVerticalStrut(6));

        // Prescription Table
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

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        finalizeBtn = ModernButton.success("✓ Finalize & Sign Consultation (Lock Record - FR22, FR23)");
        finalizeBtn.setEnabled(false);
        finalizeBtn.addActionListener(e -> handleFinalizeConsultation());

        footer.add(finalizeBtn);
        rightWorkstation.add(footer, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightWorkstation);
        add(splitPane, BorderLayout.CENTER);
    }

    public void loadDoctorQueue() {
        queueListModel.clear();
        Doctor selectedDoctor = (Doctor) doctorSelector.getSelectedItem();
        if (selectedDoctor == null) return;

        LocalDate today = LocalDate.now();
        List<Appointment> list = appointmentService.getDoctorAppointmentsForDate(selectedDoctor.getDoctorId(), today);
        for (Appointment a : list) {
            queueListModel.addElement(a);
        }

        // Reset editor
        resetConsultationEditor();
    }

    private void loadAppointmentForConsultation(Appointment app) {
        this.activeAppointment = app;
        this.activePatient = patientService.getPatientById(app.getPatientId()).orElse(null);

        if (activePatient != null) {
            patientBannerLabel.setText(activePatient.getName() + " (" + activePatient.getAge() + " yrs / "
                    + activePatient.getGender() + ") | Contact: " + activePatient.getContactNumber());
            viewHistoryBtn.setEnabled(true);
        }

        // Start or retrieve consultation
        this.activeConsultation = consultationService.startConsultation(app.getAppointmentId());

        symptomsArea.setText(activeConsultation.getSymptoms());
        diagnosisField.setText(activeConsultation.getDiagnosis());
        notesArea.setText(activeConsultation.getNotes());

        prescriptionItems.clear();
        if (activeConsultation.getPrescription() != null) {
            prescriptionItems.addAll(activeConsultation.getPrescription().getItems());
        }
        updateRxTable();

        boolean locked = activeConsultation.isCompleted();
        applyLockState(locked);
    }

    private void applyLockState(boolean locked) {
        symptomsArea.setEditable(!locked);
        diagnosisField.setEditable(!locked);
        notesArea.setEditable(!locked);
        addRxBtn.setEnabled(!locked);
        removeRxBtn.setEnabled(!locked);
        finalizeBtn.setEnabled(!locked);

        if (locked) {
            lockStatusBadge.setText("🔒 LOCKED & IMMUTABLE (FR23)");
            lockStatusBadge.setForeground(ThemeColors.SUCCESS);
        } else {
            lockStatusBadge.setText("📝 IN-PROGRESS");
            lockStatusBadge.setForeground(ThemeColors.WARNING);
        }
    }

    private void updateRxTable() {
        rxTableModel.setRowCount(0);
        for (PrescriptionItem item : prescriptionItems) {
            rxTableModel.addRow(new Object[]{
                    item.getMedicineName(),
                    item.getDosage(),
                    item.getFrequency(),
                    item.getDuration(),
                    item.getInstructions()
            });
        }
    }

    private void resetConsultationEditor() {
        activeAppointment = null;
        activePatient = null;
        activeConsultation = null;
        prescriptionItems.clear();
        updateRxTable();

        patientBannerLabel.setText("Select an appointment from the queue to start consultation (FR17).");
        viewHistoryBtn.setEnabled(false);
        symptomsArea.setText("");
        diagnosisField.setText("");
        notesArea.setText("");
        lockStatusBadge.setText(" ");
        applyLockState(true);
    }

    private void handleFinalizeConsultation() {
        if (activeConsultation == null) return;

        String diagnosis = diagnosisField.getText().trim();
        if (diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Clinical Diagnosis is mandatory to finalize consultation.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Finalizing this consultation will permanently LOCK the medical record (FR23),\n"
                        + "update appointment to COMPLETED (FR15), add to patient history (FR22),\n"
                        + "and create an automated consultation bill (FR24).\nProceed?",
                "Confirm Finalization", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // 1. Record findings (FR18)
            consultationService.recordClinicalFindings(activeConsultation.getConsultationId(),
                    symptomsArea.getText().trim(), diagnosis, notesArea.getText().trim());

            // 2. Attach prescription (FR19)
            Prescription rx = new Prescription("RX-" + activeConsultation.getConsultationId(), activeConsultation.getConsultationId());
            for (PrescriptionItem it : prescriptionItems) {
                rx.addItem(it);
            }
            consultationService.attachPrescription(activeConsultation.getConsultationId(), rx);

            // 3. Finalize & Lock Consultation (FR22, FR23, FR24)
            Consultation completedConsult = consultationService.finalizeAndCompleteConsultation(activeConsultation.getConsultationId());

            JOptionPane.showMessageDialog(this,
                    "Consultation #" + completedConsult.getConsultationId() + " successfully completed!\n"
                            + "- Medical record locked (FR23)\n"
                            + "- Added to patient permanent history (FR22)\n"
                            + "- Invoice generated for front desk billing (FR24)",
                    "Consultation Finalized", JOptionPane.INFORMATION_MESSAGE);

            loadDoctorQueue();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
