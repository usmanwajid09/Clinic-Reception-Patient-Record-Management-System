package com.clinic.ui.dialogs;

import com.clinic.model.Doctor;
import com.clinic.model.TimeSlot;
import com.clinic.service.IDoctorService;
import com.clinic.service.ValidationException;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.util.*;
import java.util.List;

/**
 * Modal dialog for configuring a doctor's working days and consultation time slots (FR8).
 */
public class DoctorScheduleDialog extends JDialog {
    private final Doctor doctor;
    private final IDoctorService doctorService;
    private boolean saved = false;

    private final Map<DayOfWeek, JCheckBox> dayBoxes = new LinkedHashMap<>();
    private final Map<TimeSlot, JCheckBox> slotBoxes = new LinkedHashMap<>();

    private static final List<TimeSlot> CANDIDATE_SLOTS = Arrays.asList(
            TimeSlot.of(9, 0, 9, 30),
            TimeSlot.of(9, 30, 10, 0),
            TimeSlot.of(10, 0, 10, 30),
            TimeSlot.of(10, 30, 11, 0),
            TimeSlot.of(11, 0, 11, 30),
            TimeSlot.of(11, 30, 12, 0),
            TimeSlot.of(14, 0, 14, 30),
            TimeSlot.of(14, 30, 15, 0),
            TimeSlot.of(15, 0, 15, 30),
            TimeSlot.of(15, 30, 16, 0),
            TimeSlot.of(16, 0, 16, 30),
            TimeSlot.of(16, 30, 17, 0)
    );

    public DoctorScheduleDialog(Window owner, Doctor doctor, IDoctorService doctorService) {
        super(owner, "Configure Schedule - " + doctor.getName(), ModalityType.APPLICATION_MODAL);
        this.doctor = doctor;
        this.doctorService = doctorService;

        initComponents();
        setSize(650, 520);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Schedule Configuration: " + doctor.getName());
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Define working days and daily consultation time slots (FR8)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Content: Left = Days, Right = Time Slots
        JPanel content = new JPanel(new GridLayout(1, 2, 16, 0));
        content.setOpaque(false);

        // Days panel
        JPanel daysCard = UIStyle.createCard();
        daysCard.setLayout(new BorderLayout(0, 10));
        JLabel daysTitle = new JLabel("Working Days (Mon - Sun):");
        daysTitle.setFont(UIStyle.FONT_BOLD);
        daysCard.add(daysTitle, BorderLayout.NORTH);

        JPanel daysBoxPanel = new JPanel(new GridLayout(7, 1, 4, 4));
        daysBoxPanel.setOpaque(false);
        for (DayOfWeek day : DayOfWeek.values()) {
            JCheckBox cb = new JCheckBox(day.toString());
            cb.setFont(UIStyle.FONT_REGULAR);
            cb.setOpaque(false);
            if (doctor.getWorkingDays().contains(day)) {
                cb.setSelected(true);
            }
            dayBoxes.put(day, cb);
            daysBoxPanel.add(cb);
        }
        daysCard.add(daysBoxPanel, BorderLayout.CENTER);
        content.add(daysCard);

        // Slots panel
        JPanel slotsCard = UIStyle.createCard();
        slotsCard.setLayout(new BorderLayout(0, 10));
        JLabel slotsTitle = new JLabel("Consultation Time Slots:");
        slotsTitle.setFont(UIStyle.FONT_BOLD);
        slotsCard.add(slotsTitle, BorderLayout.NORTH);

        JPanel slotsBoxPanel = new JPanel(new GridLayout(6, 2, 4, 4));
        slotsBoxPanel.setOpaque(false);
        for (TimeSlot slot : CANDIDATE_SLOTS) {
            JCheckBox cb = new JCheckBox(slot.getFormatted());
            cb.setFont(UIStyle.FONT_SMALL);
            cb.setOpaque(false);
            if (doctor.getConsultationSlots().contains(slot)) {
                cb.setSelected(true);
            }
            slotBoxes.put(slot, cb);
            slotsBoxPanel.add(cb);
        }
        slotsCard.add(slotsBoxPanel, BorderLayout.CENTER);
        content.add(slotsCard);

        root.add(content, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton("Save Schedule");
        saveBtn.addActionListener(e -> handleSave());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleSave() {
        Set<DayOfWeek> selectedDays = EnumSet.noneOf(DayOfWeek.class);
        for (Map.Entry<DayOfWeek, JCheckBox> entry : dayBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedDays.add(entry.getKey());
            }
        }

        List<TimeSlot> selectedSlots = new ArrayList<>();
        for (Map.Entry<TimeSlot, JCheckBox> entry : slotBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedSlots.add(entry.getKey());
            }
        }

        try {
            doctorService.setDoctorSchedule(doctor.getDoctorId(), selectedDays, selectedSlots);
            JOptionPane.showMessageDialog(this, "Schedule updated for " + doctor.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            this.saved = true;
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
