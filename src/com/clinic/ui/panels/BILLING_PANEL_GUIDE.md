# Billing, Fee Settlement & Receipts Subsystem Guide

## Overview & Functional Scope
This panel operates as the front-desk cashier terminal for patient payments, fee calculations, and receipt printing:
- **FR24 (Automatic Bill Generation)**: Generates a billing invoice for every completed consultation. Computes total amount by adding the attending doctor's consultation fee and fixed clinic service charges (`Rs. 500.00`).
- **FR25 (Payment Settlement)**: Front-desk receptionist records payment method (`Cash`, `Card`, `Online`) and updates status (`Paid`, `Unpaid`, `Partially Paid`).
- **FR26 (Printable Receipt)**: Generates an official printable receipt for settled bills via Java desktop printing architecture (`java.awt.print.Printable`).

## Component & Architecture Map
- **UI View**: `BillingManagementPanel.java`
- **Dialogs**: `PaymentDialog.java` (Payment processor), `PrintableReceiptDialog.java` (Receipt preview & printer)
- **Service**: `BillingService.java` (Implements `IBillingService`)
- **Printer Utility**: `ReceiptPrinter.java` (Page format, graphics drawing, and print job dispatch)
- **Repository**: `InMemoryBillRepository.java`
- **Domain Entities**: `Bill.java`, `PaymentStatus.java`, `PaymentMethod.java`

## Security & Segregation of Duties Rules
- **Receptionist**: Dedicated Cashier authority (Processes patient payments, issues receipts, inspects unpaid queues).
- **Doctor**: Strictly separated from cashier billing (Physicians do not handle money or bill collection).
- **Administrator**: Inaccessible for cashier counter transactions; administrators audit financial totals via Analytics & Revenue reports.

