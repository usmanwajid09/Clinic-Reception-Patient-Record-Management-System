package com.clinic.ui.dialogs;

import com.clinic.model.PrescriptionItem;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for entering prescribed medicine details (FR20, FR21).
 * Records medicine name, dosage, frequency, and duration.
 */
public class PrescriptionItemDialog extends JDialog {
    private PrescriptionItem createdItem;

    private JTextField medicineNameField;
    private JTextField dosageField;
    private JComboBox<String> frequencyCombo;
    private JTextField durationField;
    private JTextField instructionsField;

    public PrescriptionItemDialog(Window owner) {
        super(owner, "Add Prescribed Medicine", ModalityType.APPLICATION_MODAL);
        initComponents();
        pack();
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
        JLabel titleLabel = new JLabel("Prescribe Medication");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Specify medicine name, dosage, frequency, and duration (FR21)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel card = UIStyle.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        medicineNameField = UIStyle.createStyledTextField(20);
        medicineNameField.setToolTipText("e.g. Amoxicillin 500mg, Paracetamol 650mg");

        dosageField = UIStyle.createStyledTextField(20);
        dosageField.setText("1 Tablet");

        frequencyCombo = new JComboBox<>(new String[]{
                "Once daily (OD) - Morning",
                "Once daily (HS) - At bedtime",
                "Twice daily (BD) - 1-0-1",
                "Three times daily (TDS) - 1-1-1",
                "Four times daily (QID) - Every 6 hrs",
                "As needed (SOS) - For pain/fever"
        });
        frequencyCombo.setFont(UIStyle.FONT_REGULAR);
        frequencyCombo.setBackground(Color.WHITE);

        durationField = UIStyle.createStyledTextField(20);
        durationField.setText("5 days");

        instructionsField = UIStyle.createStyledTextField(20);
        instructionsField.setText("Take after meals with water.");

        int row = 0;
        addFormRow(card, gbc, row++, "Medicine Name *:", medicineNameField);
        addFormRow(card, gbc, row++, "Dosage *:", dosageField);
        addFormRow(card, gbc, row++, "Frequency *:", frequencyCombo);
        addFormRow(card, gbc, row++, "Duration *:", durationField);
        addFormRow(card, gbc, row++, "Instructions:", instructionsField);

        root.add(card, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton addBtn = new ModernButton("Add to Prescription");
        addBtn.addActionListener(e -> handleAdd());

        footer.add(cancelBtn);
        footer.add(addBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIStyle.FONT_BOLD);
        lbl.setForeground(ThemeColors.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(input, gbc);
    }

    private void handleAdd() {
        String name = medicineNameField.getText().trim();
        String dosage = dosageField.getText().trim();
        String frequency = (String) frequencyCombo.getSelectedItem();
        String duration = durationField.getText().trim();
        String instructions = instructionsField.getText().trim();

        if (name.isEmpty() || dosage.isEmpty() || duration.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Medicine Name, Dosage, and Duration are mandatory.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.createdItem = new PrescriptionItem(name, dosage, frequency, duration, instructions);
        dispose();
    }

    public PrescriptionItem getCreatedItem() {
        return createdItem;
    }
}

