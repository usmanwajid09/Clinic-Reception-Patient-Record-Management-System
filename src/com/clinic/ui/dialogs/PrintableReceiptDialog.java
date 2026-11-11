package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData.BillRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Pure UI Receipt Preview Dialog displaying formatted receipt layout.
 */
public class PrintableReceiptDialog extends JDialog {
    private final BillRow bill;

    public PrintableReceiptDialog(Window owner, BillRow bill) {
        super(owner, "Payment Receipt - " + (bill.receiptNumber != null ? bill.receiptNumber : bill.billId), ModalityType.APPLICATION_MODAL);
        this.bill = bill;

        initComponents();
        setSize(650, 620);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Official Payment Receipt", IconFactory.getBillingIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Formatted receipt preview for clinic billing desk");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(UIStyle.FONT_MONO);
        receiptArea.setText(buildReceiptPreviewText());
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setBorder(new EmptyBorder(12, 16, 12, 16));
        receiptArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton closeBtn = ModernButton.secondary("Close");
        closeBtn.addActionListener(e -> dispose());

        ModernButton printBtn = new ModernButton("Print Preview");
        printBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Print layout dialog ready for printer spool.", "Print UI", JOptionPane.INFORMATION_MESSAGE));

        footer.add(closeBtn);
        footer.add(printBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private String buildReceiptPreviewText() {
        StringBuilder sb = new StringBuilder();
        String line = "================================================================";
        String thinLine = "----------------------------------------------------------------";

        sb.append(line).append("\n");
        sb.append(centerText(UIBranding.CLINIC_NAME)).append("\n");
        sb.append(centerText(UIBranding.CLINIC_TAGLINE)).append("\n");
        sb.append(centerText(UIBranding.CLINIC_ADDRESS)).append("\n");
        sb.append(centerText(UIBranding.CLINIC_CONTACT)).append("\n");
        sb.append(line).append("\n");
        sb.append(centerText("** OFFICIAL PAYMENT RECEIPT **")).append("\n");
        sb.append(line).append("\n\n");

        sb.append(String.format("Receipt Number : %s\n", bill.receiptNumber != null ? bill.receiptNumber : "PENDING"));
        sb.append(String.format("Invoice Ref    : %s\n", bill.billId));
        sb.append(String.format("Date           : %s\n", bill.date));
        sb.append(String.format("Payment Method : %s\n", bill.method));
        sb.append(String.format("Payment Status : %s\n\n", bill.status));

        sb.append(thinLine).append("\n");
        sb.append("PATIENT DETAILS:\n");
        sb.append(String.format("  Patient ID   : %s\n", bill.patientId));
        sb.append(String.format("  Name         : %s\n", bill.patientName));
        sb.append(String.format("  Contact      : %s\n", bill.patientContact));
        sb.append(thinLine).append("\n");

        sb.append("ATTENDING CLINICIAN:\n");
        sb.append(String.format("  Doctor ID    : %s\n", bill.doctorId));
        sb.append(String.format("  Name         : %s\n", bill.doctorName));
        sb.append(String.format("  Department   : %s\n", bill.department));
        sb.append(String.format("  Specialty    : %s\n", bill.specialty));
        sb.append(thinLine).append("\n\n");

        sb.append(String.format("%-45s %15s\n", "DESCRIPTION", "AMOUNT (" + UIBranding.CURRENCY_SYMBOL + ")"));
        sb.append(thinLine).append("\n");
        sb.append(String.format("%-45s %15.2f\n", "Doctor Consultation Fee", bill.doctorFee));
        sb.append(String.format("%-45s %15.2f\n", "Fixed Clinic Service Charge", bill.clinicCharge));
        sb.append(line).append("\n");
        sb.append(String.format("%-45s %15.2f\n", "TOTAL AMOUNT:", bill.totalAmount));
        sb.append(line).append("\n\n");

        sb.append(centerText("Thank you for choosing " + UIBranding.CLINIC_NAME + ".")).append("\n");
        sb.append(line).append("\n");
        return sb.toString();
    }

    private String centerText(String text) {
        int width = 64;
        int pad = (width - text.length()) / 2;
        if (pad <= 0) return text;
        return " ".repeat(pad) + text;
    }
}
