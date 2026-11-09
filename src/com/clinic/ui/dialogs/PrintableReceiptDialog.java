package com.clinic.ui.dialogs;

import com.clinic.model.Bill;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.ReceiptPrinter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog displaying a professional printable receipt preview (FR26).
 * Triggers native system printing via Java standard AWT Printing API.
 */
public class PrintableReceiptDialog extends JDialog {
    private final Bill bill;
    private final Patient patient;
    private final Doctor doctor;
    private final ReceiptPrinter receiptPrinter;

    public PrintableReceiptDialog(Window owner, Bill bill, Patient patient, Doctor doctor) {
        super(owner, "Printable Receipt - " + (bill.getReceiptNumber() != null ? bill.getReceiptNumber() : bill.getBillId()), ModalityType.APPLICATION_MODAL);
        this.bill = bill;
        this.patient = patient;
        this.doctor = doctor;
        this.receiptPrinter = new ReceiptPrinter(bill, patient, doctor);

        initComponents();
        setSize(650, 620);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Official Payment Receipt", IconFactory.getBillingIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Printable proof of payment generated for paid consultation (FR26)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Monospaced Receipt Preview Area
        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(UIStyle.FONT_MONO);
        receiptArea.setText(receiptPrinter.generateReceiptText());
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setBorder(new EmptyBorder(12, 16, 12, 16));
        receiptArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        root.add(scrollPane, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton closeBtn = ModernButton.secondary("Close");
        closeBtn.addActionListener(e -> dispose());

        ModernButton printBtn = ModernButton.success("Print Receipt (FR26)");
        printBtn.addActionListener(e -> {
            boolean success = receiptPrinter.printReceipt();
            if (success) {
                JOptionPane.showMessageDialog(this, "Receipt successfully dispatched to printer!", "Printing", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        footer.add(closeBtn);
        footer.add(printBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
