package com.clinic.util;

import com.clinic.config.AppConfig;
import com.clinic.model.Bill;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;

import java.awt.*;
import java.awt.print.*;

/**
 * Generates formatted printable receipts for paid consultation bills (FR26).
 * Integrates with Java standard AWT Printing API (Printable).
 * Adheres to Single Responsibility Principle.
 */
public class ReceiptPrinter implements Printable {
    private final Bill bill;
    private final Patient patient;
    private final Doctor doctor;

    public ReceiptPrinter(Bill bill, Patient patient, Doctor doctor) {
        this.bill = bill;
        this.patient = patient;
        this.doctor = doctor;
    }

    /**
     * Formats receipt text for display in the preview dialog.
     */
    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        String line = "================================================================";
        String thinLine = "----------------------------------------------------------------";

        sb.append(line).append("\n");
        sb.append(centerText(AppConfig.CLINIC_NAME)).append("\n");
        sb.append(centerText(AppConfig.CLINIC_TAGLINE)).append("\n");
        sb.append(centerText(AppConfig.CLINIC_ADDRESS)).append("\n");
        sb.append(centerText(AppConfig.CLINIC_CONTACT)).append("\n");
        sb.append(line).append("\n");
        sb.append(centerText("** OFFICIAL PAYMENT RECEIPT **")).append("\n");
        sb.append(line).append("\n\n");

        sb.append(String.format("Receipt Number : %s\n", bill.getReceiptNumber() != null ? bill.getReceiptNumber() : "N/A"));
        sb.append(String.format("Invoice Ref    : %s\n", bill.getBillId()));
        sb.append(String.format("Date & Time    : %s\n", DateTimeUtil.formatDateTime(bill.getPaidAt() != null ? bill.getPaidAt() : bill.getBilledAt())));
        sb.append(String.format("Payment Method : %s\n", bill.getPaymentMethod() != null ? bill.getPaymentMethod().getDisplayName() : "N/A"));
        sb.append(String.format("Payment Status : %s\n\n", bill.getPaymentStatus().getDisplayName().toUpperCase()));

        sb.append(thinLine).append("\n");
        sb.append("PATIENT DETAILS:\n");
        sb.append(String.format("  Patient ID   : %s\n", patient != null ? patient.getPatientId() : bill.getPatientId()));
        sb.append(String.format("  Name         : %s\n", patient != null ? patient.getName() : "N/A"));
        sb.append(String.format("  Contact      : %s\n", patient != null ? patient.getContactNumber() : "N/A"));
        sb.append(thinLine).append("\n");

        sb.append("ATTENDING CLINICIAN:\n");
        sb.append(String.format("  Doctor ID    : %s\n", doctor != null ? doctor.getDoctorId() : bill.getDoctorId()));
        sb.append(String.format("  Name         : %s\n", doctor != null ? doctor.getName() : "N/A"));
        sb.append(String.format("  Department   : %s\n", doctor != null ? doctor.getDepartment().getName() : "N/A"));
        sb.append(String.format("  Specialty    : %s\n", doctor != null ? doctor.getSpecialization() : "N/A"));
        sb.append(thinLine).append("\n\n");

        sb.append(String.format("%-45s %15s\n", "DESCRIPTION", "AMOUNT (" + AppConfig.CURRENCY_SYMBOL + ")"));
        sb.append(thinLine).append("\n");
        sb.append(String.format("%-45s %15.2f\n", "Doctor Consultation Fee", bill.getDoctorConsultationFee()));
        sb.append(String.format("%-45s %15.2f\n", "Fixed Clinic Administrative/Facility Charge", bill.getClinicServiceCharge()));
        sb.append(line).append("\n");
        sb.append(String.format("%-45s %15.2f\n", "TOTAL AMOUNT PAID:", bill.getTotalAmount()));
        sb.append(line).append("\n\n");

        sb.append(centerText("Thank you for choosing " + AppConfig.CLINIC_NAME + ".")).append("\n");
        sb.append(centerText("Wishing you good health and a speedy recovery.")).append("\n");
        sb.append(line).append("\n");

        return sb.toString();
    }

    private String centerText(String text) {
        int width = 64;
        int padSize = (width - text.length()) / 2;
        if (padSize <= 0) return text;
        return " ".repeat(padSize) + text;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));

        int y = 20;
        String[] lines = generateReceiptText().split("\n");
        for (String lineText : lines) {
            g2d.drawString(lineText, 10, y);
            y += 12;
        }

        return PAGE_EXISTS;
    }

    /**
     * Triggers the native printer dialog to print this receipt.
     */
    public boolean printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try {
                job.print();
                return true;
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
