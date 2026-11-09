package com.clinic.ui.panels;

import com.clinic.config.AppConfig;
import com.clinic.model.Bill;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.model.PaymentStatus;
import com.clinic.service.IBillingService;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.dialogs.PaymentDialog;
import com.clinic.ui.dialogs.PrintableReceiptDialog;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;
import com.clinic.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Presentation panel for Clinic Invoicing, Payment Collection, and Receipts (FR24, FR25, FR26).
 */
public class BillingManagementPanel extends JPanel {
    private final IBillingService billingService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;

    private JTable billTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;

    private StatCard totalInvoicedCard;
    private StatCard totalPaidCard;
    private StatCard totalOutstandingCard;

    public BillingManagementPanel(IBillingService billingService,
                                  IPatientService patientService,
                                  IDoctorService doctorService) {
        this.billingService = billingService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadBills();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        // Header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Billing, Fee Settlement & Receipts");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Automatic consultation bills (FR24), payment processing (FR25), and printable official receipts (FR26)");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Stats Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        statsPanel.setOpaque(false);

        totalInvoicedCard = new StatCard("Total Invoiced", "$0.00", "Consultation fees + charges", ThemeColors.INFO);
        totalPaidCard = new StatCard("Collected Revenue", "$0.00", "Settled payments (FR25)", ThemeColors.SUCCESS);
        totalOutstandingCard = new StatCard("Pending Balance", "$0.00", "Awaiting collection", ThemeColors.WARNING);

        statsPanel.add(totalInvoicedCard);
        statsPanel.add(totalPaidCard);
        statsPanel.add(totalOutstandingCard);

        // Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setOpaque(false);
        JLabel statusLbl = new JLabel("Payment Status Filter (FR25):");
        statusLbl.setFont(UIStyle.FONT_BOLD);
        statusFilter = new JComboBox<>(new String[]{"All Bills", "Unpaid", "Paid"});
        statusFilter.setFont(UIStyle.FONT_REGULAR);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> filterBills());

        filterBox.add(statusLbl);
        filterBox.add(statusFilter);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        ModernButton payBtn = ModernButton.success("Record Payment (FR25)");
        payBtn.addActionListener(e -> handleRecordPayment());

        ModernButton receiptBtn = ModernButton.secondary("Print Receipt (FR26)");
        receiptBtn.addActionListener(e -> handlePrintReceipt());

        btnBox.add(receiptBtn);
        btnBox.add(payBtn);

        toolbar.add(filterBox, BorderLayout.WEST);
        toolbar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Invoice ID", "Consultation", "Patient", "Doctor", "Doctor Fee (FR24)", "Clinic Charge (FR24)", "Total Amount", "Status (FR25)", "Receipt Ref (FR26)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        billTable = new JTable(tableModel);
        UIStyle.styleTable(billTable);
        billTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        billTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        billTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        billTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        billTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        billTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        billTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        billTable.getColumnModel().getColumn(7).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadBills() {
        filterBills();
    }

    private void filterBills() {
        tableModel.setRowCount(0);
        String selected = (String) statusFilter.getSelectedItem();

        List<Bill> list = billingService.getAllBills();
        double totalInvoiced = 0.0;
        double totalPaid = 0.0;
        double totalOutstanding = 0.0;

        for (Bill b : list) {
            totalInvoiced += b.getTotalAmount();
            if (b.isPaid()) {
                totalPaid += b.getTotalAmount();
            } else {
                totalOutstanding += b.getTotalAmount();
            }

            if ("Unpaid".equalsIgnoreCase(selected) && b.isPaid()) continue;
            if ("Paid".equalsIgnoreCase(selected) && !b.isPaid()) continue;

            Patient p = patientService.getPatientById(b.getPatientId()).orElse(null);
            Doctor d = doctorService.getDoctorById(b.getDoctorId()).orElse(null);

            tableModel.addRow(new Object[]{
                    b.getBillId(),
                    b.getConsultationId(),
                    p != null ? p.getName() : b.getPatientId(),
                    d != null ? d.getName() : b.getDoctorId(),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", b.getDoctorConsultationFee()),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", b.getClinicServiceCharge()),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", b.getTotalAmount()),
                    b.getPaymentStatus().getDisplayName(),
                    b.getReceiptNumber() != null ? b.getReceiptNumber() : "-"
            });
        }

        totalInvoicedCard.setValue(AppConfig.CURRENCY_SYMBOL + String.format("%.2f", totalInvoiced));
        totalPaidCard.setValue(AppConfig.CURRENCY_SYMBOL + String.format("%.2f", totalPaid));
        totalOutstandingCard.setValue(AppConfig.CURRENCY_SYMBOL + String.format("%.2f", totalOutstanding));
    }

    private void handleRecordPayment() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an unpaid bill from the table.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String billId = (String) tableModel.getValueAt(row, 0);
        billingService.getBillById(billId).ifPresent(bill -> {
            if (bill.isPaid()) {
                JOptionPane.showMessageDialog(this, "This bill is already settled (Status: PAID).", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            PaymentDialog dialog = new PaymentDialog(SwingUtilities.getWindowAncestor(this), bill,
                    billingService, patientService, doctorService);
            dialog.setVisible(true);
            if (dialog.isPaid()) {
                loadBills();
            }
        });
    }

    private void handlePrintReceipt() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the table to print receipt.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String billId = (String) tableModel.getValueAt(row, 0);
        billingService.getBillById(billId).ifPresent(bill -> {
            Patient p = patientService.getPatientById(bill.getPatientId()).orElse(null);
            Doctor d = doctorService.getDoctorById(bill.getDoctorId()).orElse(null);

            PrintableReceiptDialog dialog = new PrintableReceiptDialog(SwingUtilities.getWindowAncestor(this), bill, p, d);
            dialog.setVisible(true);
        });
    }
}
