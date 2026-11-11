package com.clinic.ui.dialogs;

import com.clinic.ui.components.ModernButton;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.BillRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal UI dialog for displaying invoice breakdown and choosing payment method.
 */
public class PaymentDialog extends JDialog {
    private final BillRow bill;
    private boolean paid = false;
    private JComboBox<String> methodCombo;

    public PaymentDialog(Window owner, BillRow bill) {
        super(owner, "Process Payment - Invoice #" + bill.billId, ModalityType.APPLICATION_MODAL);
        this.bill = bill;

        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(ThemeColors.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Record Consultation Payment", IconFactory.getBillingIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Review fee breakdown and select payment method");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        JPanel card = UIStyle.createCard();
        card.setLayout(new GridLayout(6, 2, 8, 8));

        addSummaryRow(card, "Invoice Number:", bill.billId);
        addSummaryRow(card, "Patient Name:", bill.patientName);
        addSummaryRow(card, "Attending Doctor:", bill.doctorName);
        addSummaryRow(card, "Consultation Fee:", UIBranding.CURRENCY_SYMBOL + String.format("%.2f", bill.doctorFee));
        addSummaryRow(card, "Clinic Service Charge:", UIBranding.CURRENCY_SYMBOL + String.format("%.2f", bill.clinicCharge));
        addSummaryRow(card, "Total Amount Due:", UIBranding.CURRENCY_SYMBOL + String.format("%.2f", bill.totalAmount));

        JPanel methodPanel = UIStyle.createCard();
        methodPanel.setLayout(new BorderLayout(8, 8));
        JLabel methodLabel = new JLabel("Payment Method:");
        methodLabel.setFont(UIStyle.FONT_BOLD);
        methodPanel.add(methodLabel, BorderLayout.WEST);

        methodCombo = new JComboBox<>(new String[]{"Cash", "Card", "Online Transfer"});
        methodCombo.setFont(UIStyle.FONT_REGULAR);
        methodCombo.setBackground(Color.WHITE);
        methodPanel.add(methodCombo, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(card, BorderLayout.CENTER);
        centerPanel.add(methodPanel, BorderLayout.SOUTH);
        root.add(centerPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton payBtn = ModernButton.success("Mark as Paid (" + UIBranding.CURRENCY_SYMBOL + String.format("%.2f", bill.totalAmount) + ")");
        payBtn.addActionListener(e -> handlePayment());

        footer.add(cancelBtn);
        footer.add(payBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addSummaryRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(UIStyle.FONT_REGULAR);
        l.setForeground(ThemeColors.TEXT_SECONDARY);

        JLabel v = new JLabel(value);
        v.setFont(UIStyle.FONT_BOLD);
        v.setForeground(ThemeColors.TEXT_PRIMARY);

        panel.add(l);
        panel.add(v);
    }

    private void handlePayment() {
        bill.status = "PAID";
        bill.method = (String) methodCombo.getSelectedItem();
        if (bill.receiptNumber == null) {
            bill.receiptNumber = MockClinicData.nextReceiptNumber();
        }
        this.paid = true;
        dispose();

        int choice = JOptionPane.showConfirmDialog(getOwner(),
                "Invoice marked as PAID.\nOpen printable receipt preview?",
                "Payment Recorded", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            new PrintableReceiptDialog(getOwner(), bill).setVisible(true);
        }
    }

    public boolean isPaid() {
        return paid;
    }
}
