package com.clinic.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a discrete consultation time slot for doctor appointments (FR8).
 */
public class TimeSlot implements Comparable<TimeSlot> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final LocalTime startTime;
    private final LocalTime endTime;

    public TimeSlot(LocalTime startTime, LocalTime endTime) {
        this.startTime = Objects.requireNonNull(startTime, "startTime cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "endTime cannot be null");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    public static TimeSlot of(int startHour, int startMinute, int endHour, int endMinute) {
        return new TimeSlot(LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute));
    }

    public static TimeSlot parse(String slotString) {
        // Expects format "HH:mm - HH:mm" or "hh:mm a - hh:mm a"
        String[] parts = slotString.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid time slot format. Expected 'HH:mm - HH:mm'");
        }
        LocalTime start = LocalTime.parse(parts[0].trim());
        LocalTime end = LocalTime.parse(parts[1].trim());
        return new TimeSlot(start, end);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getFormatted() {
        return startTime.format(FORMATTER) + " - " + endTime.format(FORMATTER);
    }

    /**
     * Checks if this slot overlaps with another slot.
     */
    public boolean overlaps(TimeSlot other) {
        if (other == null) return false;
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot timeSlot)) return false;
        return Objects.equals(startTime, timeSlot.startTime) && Objects.equals(endTime, timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    @Override
    public int compareTo(TimeSlot o) {
        return this.startTime.compareTo(o.startTime);
    }

    @Override
    public String toString() {
        return getFormatted();
    }
}
