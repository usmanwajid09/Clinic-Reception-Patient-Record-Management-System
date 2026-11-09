package com.clinic.repository.inmemory;

import com.clinic.model.Bill;
import com.clinic.model.PaymentStatus;
import com.clinic.repository.IBillRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of IBillRepository.
 */
public class InMemoryBillRepository implements IBillRepository {
    private final Map<String, Bill> storage = new ConcurrentHashMap<>();
    private final AtomicInteger billSequence = new AtomicInteger(3000);
    private final AtomicInteger receiptSequence = new AtomicInteger(8000);

    @Override
    public Bill save(Bill bill) {
        Objects.requireNonNull(bill, "bill cannot be null");
        storage.put(bill.getBillId().toUpperCase(), bill);
        return bill;
    }

    @Override
    public Optional<Bill> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id.trim().toUpperCase()));
    }

    @Override
    public List<Bill> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        return storage.containsKey(id.trim().toUpperCase());
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            storage.remove(id.trim().toUpperCase());
        }
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public Optional<Bill> findByConsultationId(String consultationId) {
        if (consultationId == null) return Optional.empty();

        return storage.values().stream()
                .filter(b -> b.getConsultationId().equalsIgnoreCase(consultationId.trim()))
                .findFirst();
    }

    @Override
    public List<Bill> findByPaymentStatus(PaymentStatus status) {
        if (status == null) return findAll();

        return storage.values().stream()
                .filter(b -> b.getPaymentStatus() == status)
                .sorted(Comparator.comparing(Bill::getBilledAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        return storage.values().stream()
                .filter(b -> {
                    LocalDate billDate = b.getBilledAt().toLocalDate();
                    return !billDate.isBefore(start) && !billDate.isAfter(end);
                })
                .sorted(Comparator.comparing(Bill::getBilledAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String generateNextBillId() {
        return "INV-" + billSequence.incrementAndGet();
    }

    @Override
    public String generateNextReceiptNumber() {
        return "REC-" + receiptSequence.incrementAndGet();
    }
}

// Atomic sequence increments for invoice IDs and receipts
