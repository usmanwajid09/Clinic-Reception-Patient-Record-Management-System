package com.clinic.ui.panels;

import com.clinic.config.AppConfig;
import com.clinic.model.Bill;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.service.IBillingService;
import com.clinic.service.IDoctorService;
import com.clinic.service.IPatientService;
import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.dialogs.PaymentDialog;
import com.clinic.ui.dialogs.PrintableReceiptDialog;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Presentation panel for Consultation Invoicing, Fee Collection, and Receipts (FR24 - FR26).
 * Front desk cashier terminal for patient payments and thermal receipt generation.
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

    public BillingManagementPanel(IBillingService billingService, IPatientService patientService, IDoctorService doctorService) {
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

        totalInvoicedCard = new StatCard("Total Invoiced", "Rs. 0.00", "Consultation fees + clinic charges", ThemeColors.INFO);
        totalPaidCard = new StatCard("Collected Revenue", "Rs. 0.00", "Settled payments (FR25)", ThemeColors.SUCCESS);
        totalOutstandingCard = new StatCard("Pending Balance", "Rs. 0.00", "Awaiting collection", ThemeColors.WARNING);

        statsPanel.add(totalInvoicedCard);
        statsPanel.add(totalPaidCard);
        statsPanel.add(totalOutstandingCard);

        // Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setOpaque(false);

        JLabel filterLabel = new JLabel("Status Filter:");
        filterLabel.setFont(UIStyle.FONT_BOLD);
        statusFilter = new JComboBox<>(new String[]{"All Bills", "Unpaid Only", "Paid Only"});
        statusFilter.setFont(UIStyle.FONT_REGULAR);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> filterBills());

        filterBox.add(filterLabel);
        filterBox.add(statusFilter);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        ModernButton payBtn = new ModernButton("💳 Record Payment (FR25)");
        payBtn.addActionListener(e -> handleRecordPayment());

        ModernButton receiptBtn = ModernButton.secondary("🖨️ View / Print Receipt (FR26)");
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
        String[] columns = {"Bill ID", "Date", "Patient Name", "Clinician", "Total Amount (FR24)", "Payment Status (FR25)", "Paid Amount", "Method"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        billTable = new JTable(tableModel);
        UIStyle.styleTable(billTable);
        UIStyle.applyStatusBadge(billTable, 5);

        billTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        billTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        billTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        billTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        billTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        billTable.getColumnModel().getColumn(5).setPreferredWidth(105);
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
        String filter = (String) statusFilter.getSelectedItem();
        List<Bill> list;

        if ("Unpaid Only".equals(filter)) {
            list = billingService.getBillsByStatus(com.clinic.model.PaymentStatus.UNPAID);
        } else if ("Paid Only".equals(filter)) {
            list = billingService.getAllBills().stream().filter(Bill::isPaid).toList();
        } else {
            list = billingService.getAllBills();
        }

        renderBills(list);
    }

    private void renderBills(List<Bill> list) {
        tableModel.setRowCount(0);

        double totalInvoiced = 0.0;
        double totalPaid = 0.0;
        double totalOutstanding = 0.0;

        for (Bill b : list) {
            Patient p = patientService.getPatientById(b.getPatientId()).orElse(null);
            Doctor d = doctorService.getDoctorById(b.getDoctorId()).orElse(null);

            double paid = b.isPaid() ? b.getTotalAmount() : 0.0;
            double balance = b.isPaid() ? 0.0 : b.getTotalAmount();

            totalInvoiced += b.getTotalAmount();
            totalPaid += paid;
            totalOutstanding += balance;

            tableModel.addRow(new Object[]{
                    b.getBillId(),
                    b.getBilledAt().toLocalDate().toString(),
                    p != null ? p.getName() : b.getPatientId(),
                    d != null ? d.getName() : b.getDoctorId(),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", b.getTotalAmount()),
                    b.getPaymentStatus().name(),
                    AppConfig.CURRENCY_SYMBOL + String.format("%.2f", paid),
                    b.getPaymentMethod() != null ? b.getPaymentMethod().name() : "N/A"
            });
        }

        totalInvoicedCard.setValue(AppConfig.CURRENCY_SYMBOL + String.format("%.2f", totalInvoiced));
        totalPaidCard.setValue(AppConfig.CURRENCY_SYMBOL + String.format("%.2f", totalPaid));
        totalOutstandingCard.setValue(AppConfig.CURRENCY_SYMBOL + String.format("%.2f", totalOutstanding));
    }

    private void handleRecordPayment() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select an unpaid bill from the table.");
            return;
        }

        String billId = (String) tableModel.getValueAt(row, 0);
        billingService.getBillById(billId).ifPresent(bill -> {
            if (bill.isPaid()) {
                ToastNotification.showInfo(this, "Already Settled", "This bill has already been marked as PAID.");
                return;
            }

            PaymentDialog dialog = new PaymentDialog(SwingUtilities.getWindowAncestor(this), bill,
                    billingService, patientService, doctorService);
            dialog.setVisible(true);
            if (dialog.isPaid()) {
                loadBills();
                ToastNotification.showSuccess(this, "Payment Recorded", "Bill #" + billId + " successfully marked as PAID.");
            }
        });
    }

    private void handlePrintReceipt() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select a bill from the table to print receipt.");
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
