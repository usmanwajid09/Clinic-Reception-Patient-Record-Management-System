package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.DoctorRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Modal UI dialog for configuring working days and consultation slots visually.
 */
public class DoctorScheduleDialog extends JDialog {
    private static final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

    private final DoctorRow doctor;
    private boolean saved = false;

    private final Map<String, JCheckBox> dayBoxes = new LinkedHashMap<>();
    private final Map<String, JCheckBox> slotBoxes = new LinkedHashMap<>();

    public DoctorScheduleDialog(Window owner, DoctorRow doctor) {
        super(owner, "Configure Schedule - " + doctor.name, ModalityType.APPLICATION_MODAL);
        this.doctor = doctor;

        initComponents();
        setSize(660, 520);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Schedule Configuration: " + doctor.name, IconFactory.getDoctorsIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Define working days and daily consultation time slots");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 16, 0));
        content.setOpaque(false);

        JPanel daysCard = UIStyle.createCard();
        daysCard.setLayout(new BorderLayout(0, 10));
        JLabel daysTitle = new JLabel("Working Days (Mon - Sun):");
        daysTitle.setFont(UIStyle.FONT_BOLD);
        daysCard.add(daysTitle, BorderLayout.NORTH);

        JPanel daysBoxPanel = new JPanel(new GridLayout(7, 1, 4, 4));
        daysBoxPanel.setOpaque(false);
        for (String day : DAYS) {
            JCheckBox cb = new JCheckBox(day);
            cb.setFont(UIStyle.FONT_REGULAR);
            cb.setOpaque(false);
            if (doctor.workingDays.toUpperCase().contains(day.substring(0, 3))) {
                cb.setSelected(true);
            }
            dayBoxes.put(day, cb);
            daysBoxPanel.add(cb);
        }
        daysCard.add(daysBoxPanel, BorderLayout.CENTER);
        content.add(daysCard);

        JPanel slotsCard = UIStyle.createCard();
        slotsCard.setLayout(new BorderLayout(0, 10));
        JLabel slotsTitle = new JLabel("Consultation Time Slots:");
        slotsTitle.setFont(UIStyle.FONT_BOLD);
        slotsCard.add(slotsTitle, BorderLayout.NORTH);

        JPanel slotsBoxPanel = new JPanel(new GridLayout(6, 2, 4, 4));
        slotsBoxPanel.setOpaque(false);
        for (String slot : MockClinicData.STANDARD_SLOTS) {
            JCheckBox cb = new JCheckBox(slot);
            cb.setFont(UIStyle.FONT_SMALL);
            cb.setOpaque(false);
            if (doctor.slots.contains(slot)) {
                cb.setSelected(true);
            }
            slotBoxes.put(slot, cb);
            slotsBoxPanel.add(cb);
        }
        slotsCard.add(slotsBoxPanel, BorderLayout.CENTER);
        content.add(slotsCard);

        root.add(content, BorderLayout.CENTER);

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
        List<String> selectedDays = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : dayBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedDays.add(entry.getKey().substring(0, 3));
            }
        }

        List<String> selectedSlots = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : slotBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedSlots.add(entry.getKey());
            }
        }

        doctor.workingDays = selectedDays.isEmpty() ? "None" : String.join(", ", selectedDays);
        if (!selectedSlots.isEmpty()) {
            doctor.slots = selectedSlots;
        }

        this.saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
