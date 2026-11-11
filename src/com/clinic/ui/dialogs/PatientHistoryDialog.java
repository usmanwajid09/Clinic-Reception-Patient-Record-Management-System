package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.HistoryRow;
import com.clinic.ui.mock.MockClinicData.PatientRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modal UI dialog displaying sample patient visit history.
 */
public class PatientHistoryDialog extends JDialog {
    private final PatientRow patient;

    public PatientHistoryDialog(Window owner, PatientRow patient) {
        super(owner, "Medical History - " + patient.name, ModalityType.APPLICATION_MODAL);
        this.patient = patient;

        initComponents();
        setSize(860, 520);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Patient Summary Header Card
        JPanel infoPanel = UIStyle.createCard();
        infoPanel.setLayout(new GridLayout(2, 3, 10, 6));

        addInfoItem(infoPanel, "Patient ID", patient.id);
        addInfoItem(infoPanel, "Full Name", patient.name);
        addInfoItem(infoPanel, "DOB / Gender", patient.dob + " / " + patient.gender);
        addInfoItem(infoPanel, "Contact", patient.phone);
        addInfoItem(infoPanel, "Emergency Contact", patient.emergencyContact);
        addInfoItem(infoPanel, "Address", patient.address);

        root.add(infoPanel, BorderLayout.NORTH);

        List<HistoryRow> history = MockClinicData.getHistoryForPatient(patient.id);
        String[] columns = {"Date & Time", "Doctor", "Department", "Symptoms", "Diagnosis", "Prescription"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (HistoryRow h : history) {
            model.addRow(new Object[]{h.dateTime(), h.doctorName(), h.department(), h.symptoms(), h.diagnosis(), h.rxSummary()});
        }

        JTable table = new JTable(model);
        UIStyle.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableWrapper = new JPanel(new BorderLayout(0, 8));
        tableWrapper.setOpaque(false);
        JLabel historyLabel = new JLabel("Past Consultations & Visit Records (" + history.size() + ")", IconFactory.getPatientsIcon(24), SwingConstants.LEFT);
        historyLabel.setIconTextGap(8);
        historyLabel.setFont(UIStyle.FONT_HEADER);
        historyLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        tableWrapper.add(historyLabel, BorderLayout.NORTH);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        root.add(tableWrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        ModernButton closeBtn = ModernButton.secondary("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addInfoItem(JPanel panel, String label, String value) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(UIStyle.FONT_SMALL);
        l.setForeground(ThemeColors.TEXT_SECONDARY);

        JLabel v = new JLabel(value != null ? value : "N/A");
        v.setFont(UIStyle.FONT_BOLD);
        v.setForeground(ThemeColors.TEXT_PRIMARY);

        item.add(l, BorderLayout.NORTH);
        item.add(v, BorderLayout.CENTER);
        panel.add(item);
    }
}
