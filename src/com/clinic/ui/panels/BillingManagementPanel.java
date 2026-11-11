package com.clinic.ui.panels;

import com.clinic.ui.animation.ToastNotification;
import com.clinic.ui.components.ModernButton;
import com.clinic.ui.components.StatCard;
import com.clinic.ui.dialogs.PaymentDialog;
import com.clinic.ui.dialogs.PrintableReceiptDialog;
import com.clinic.ui.mock.MockClinicData;
import com.clinic.ui.mock.MockClinicData.BillRow;
import com.clinic.ui.theme.IconFactory;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIBranding;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Presentation panel for Front-Desk Billing, Fee Breakdown Display, and Receipt Preview UI.
 */
public class BillingManagementPanel extends JPanel {
    private JTable billTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;

    private StatCard totalInvoicedCard;
    private StatCard totalPaidCard;
    private StatCard totalOutstandingCard;

    public BillingManagementPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_LIGHT);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        loadBills();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Billing, Fee Settlement & Receipts", IconFactory.getBillingIcon(24), SwingConstants.LEFT);
        titleLabel.setIconTextGap(10);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Inspect consultation invoices, record payment status, and preview official receipts");
        subtitleLabel.setFont(UIStyle.FONT_SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        statsPanel.setOpaque(false);

        totalInvoicedCard = new StatCard("Total Invoiced", UIBranding.CURRENCY_SYMBOL + "0.00", "Consultation & clinic fees", ThemeColors.INFO);
        totalPaidCard = new StatCard("Collected Revenue", UIBranding.CURRENCY_SYMBOL + "0.00", "Settled invoices", ThemeColors.SUCCESS);
        totalOutstandingCard = new StatCard("Pending Balance", UIBranding.CURRENCY_SYMBOL + "0.00", "Awaiting collection", ThemeColors.WARNING);

        statsPanel.add(totalInvoicedCard);
        statsPanel.add(totalPaidCard);
        statsPanel.add(totalOutstandingCard);

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

        ModernButton payBtn = new ModernButton("Record Payment");
        payBtn.addActionListener(e -> handleRecordPayment());

        ModernButton receiptBtn = ModernButton.secondary("Preview Receipt");
        receiptBtn.addActionListener(e -> handlePrintReceipt());

        btnBox.add(receiptBtn);
        btnBox.add(payBtn);

        toolbar.add(filterBox, BorderLayout.WEST);
        toolbar.add(btnBox, BorderLayout.EAST);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Bill ID", "Date", "Patient Name", "Clinician", "Total Amount", "Payment Status", "Paid Amount", "Method"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        billTable = new JTable(tableModel);
        UIStyle.styleTable(billTable);
        UIStyle.applyStatusBadge(billTable, 5);

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
        tableModel.setRowCount(0);

        double totalInvoiced = 0.0;
        double totalPaid = 0.0;
        double totalOutstanding = 0.0;

        for (BillRow b : MockClinicData.getBills()) {
            if ("Unpaid Only".equals(filter) && !"UNPAID".equalsIgnoreCase(b.status)) continue;
            if ("Paid Only".equals(filter) && !"PAID".equalsIgnoreCase(b.status)) continue;

            boolean isPaid = "PAID".equalsIgnoreCase(b.status);
            double paid = isPaid ? b.totalAmount : 0.0;
            double balance = isPaid ? 0.0 : b.totalAmount;

            totalInvoiced += b.totalAmount;
            totalPaid += paid;
            totalOutstanding += balance;

            tableModel.addRow(new Object[]{
                    b.billId,
                    b.date,
                    b.patientName,
                    b.doctorName,
                    UIBranding.CURRENCY_SYMBOL + String.format("%.2f", b.totalAmount),
                    b.status,
                    UIBranding.CURRENCY_SYMBOL + String.format("%.2f", paid),
                    b.method
            });
        }

        totalInvoicedCard.setValue(UIBranding.CURRENCY_SYMBOL + String.format("%.2f", totalInvoiced));
        totalPaidCard.setValue(UIBranding.CURRENCY_SYMBOL + String.format("%.2f", totalPaid));
        totalOutstandingCard.setValue(UIBranding.CURRENCY_SYMBOL + String.format("%.2f", totalOutstanding));
    }

    private BillRow findBillAtRow(int row) {
        String billId = (String) tableModel.getValueAt(row, 0);
        return MockClinicData.getBills().stream().filter(b -> b.billId.equals(billId)).findFirst().orElse(null);
    }

    private void handleRecordPayment() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select an unpaid bill from the table.");
            return;
        }
        BillRow bill = findBillAtRow(row);
        if (bill == null) return;
        if ("PAID".equalsIgnoreCase(bill.status)) {
            ToastNotification.showInfo(this, "Already Settled", "This invoice is already marked as PAID.");
            return;
        }

        PaymentDialog dialog = new PaymentDialog(SwingUtilities.getWindowAncestor(this), bill);
        dialog.setVisible(true);
        if (dialog.isPaid()) {
            loadBills();
            ToastNotification.showSuccess(this, "Payment Recorded", "Invoice " + bill.billId + " updated to PAID in UI view.");
        }
    }

    private void handlePrintReceipt() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            ToastNotification.showWarning(this, "Selection Required", "Please select an invoice row to preview receipt.");
            return;
        }
        BillRow bill = findBillAtRow(row);
        if (bill != null) {
            new PrintableReceiptDialog(SwingUtilities.getWindowAncestor(this), bill).setVisible(true);
        }
    }
}
