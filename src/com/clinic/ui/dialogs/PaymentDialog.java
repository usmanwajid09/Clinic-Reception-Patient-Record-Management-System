package com.clinic.ui.dialogs;

import com.clinic.config.AppConfig;
import com.clinic.model.Bill;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.model.PaymentMethod;
import com.clinic.service.IBillingService;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for recording bill payment and selecting settlement method (FR25).
 */
public class PaymentDialog extends JDialog {
    private final Bill bill;
    private final IBillingService billingService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    private boolean paid = false;

    private JComboBox<PaymentMethod> methodCombo;

    public PaymentDialog(Window owner, Bill bill, IBillingService billingService,
                         IPatientService patientService, IDoctorService doctorService) {
        super(owner, "Process Payment - Invoice #" + bill.getBillId(), ModalityType.APPLICATION_MODAL);
        this.bill = bill;
        this.billingService = billingService;
        this.patientService = patientService;
        this.doctorService = doctorService;

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
        JLabel titleLabel = new JLabel("Record Consultation Payment");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Collect patient fees and generate printable receipt (FR25, FR26)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // Details Breakdown Card
        JPanel card = UIStyle.createCard();
        card.setLayout(new GridLayout(6, 2, 8, 8));

        Patient patient = patientService.getPatientById(bill.getPatientId()).orElse(null);
        Doctor doctor = doctorService.getDoctorById(bill.getDoctorId()).orElse(null);

        addSummaryRow(card, "Invoice Number:", bill.getBillId());
        addSummaryRow(card, "Patient Name:", patient != null ? patient.getName() : bill.getPatientId());
        addSummaryRow(card, "Attending Doctor:", doctor != null ? doctor.getName() : bill.getDoctorId());
        addSummaryRow(card, "Consultation Fee:", AppConfig.CURRENCY_SYMBOL + String.format("%.2f", bill.getDoctorConsultationFee()));
        addSummaryRow(card, "Fixed Clinic Charge:", AppConfig.CURRENCY_SYMBOL + String.format("%.2f", bill.getClinicServiceCharge()));
        addSummaryRow(card, "Total Amount Due:", AppConfig.CURRENCY_SYMBOL + String.format("%.2f", bill.getTotalAmount()));

        root.add(card, BorderLayout.CENTER);

        // Payment Method Selector
        JPanel methodPanel = UIStyle.createCard();
        methodPanel.setLayout(new BorderLayout(8, 8));
        JLabel methodLabel = new JLabel("Payment Method *:");
        methodLabel.setFont(UIStyle.FONT_BOLD);
        methodPanel.add(methodLabel, BorderLayout.WEST);

        methodCombo = new JComboBox<>(PaymentMethod.values());
        methodCombo.setFont(UIStyle.FONT_REGULAR);
        methodCombo.setBackground(Color.WHITE);
        methodPanel.add(methodCombo, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(card, BorderLayout.CENTER);
        centerPanel.add(methodPanel, BorderLayout.SOUTH);
        root.add(centerPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ModernButton payBtn = ModernButton.success("Record Payment (" + AppConfig.CURRENCY_SYMBOL + String.format("%.2f", bill.getTotalAmount()) + ")");
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
        PaymentMethod method = (PaymentMethod) methodCombo.getSelectedItem();
        Bill updatedBill = billingService.recordPayment(bill.getBillId(), method);

        this.paid = true;
        dispose();

        // Ask to show printable receipt immediately (FR26)
        int choice = JOptionPane.showConfirmDialog(getOwner(),
                "Payment successfully recorded with status PAID!\nWould you like to preview/print the receipt now (FR26)?",
                "Payment Complete", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            Patient patient = patientService.getPatientById(updatedBill.getPatientId()).orElse(null);
            Doctor doctor = doctorService.getDoctorById(updatedBill.getDoctorId()).orElse(null);
            new PrintableReceiptDialog(getOwner(), updatedBill, patient, doctor).setVisible(true);
        }
    }

    public boolean isPaid() {
        return paid;
    }
}

