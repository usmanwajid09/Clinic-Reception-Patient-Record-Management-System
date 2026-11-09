package com.clinic.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract Base class for Users demonstrating the Liskov Substitution Principle (LSP).
 * All user subtypes are safely substitutable without altering program correctness.
 */
public abstract class User {
    private final String userId;
    private final String username;
    private final String fullName;
    private final Role role;
    private final LocalDateTime createdAt;
    private boolean active;

    protected User(String userId, String username, String fullName, Role role) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "fullName cannot be null");
        this.role = Objects.requireNonNull(role, "role cannot be null");
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Checks if this user has permission to perform an administrative action.
     */
    public abstract boolean hasAdministrativeAccess();

    /**
     * Checks if this user can conduct medical consultations.
     */
    public abstract boolean canConductConsultations();

    /**
     * Checks if this user can register patients and book appointments.
     */
    public abstract boolean canManageFrontDesk();

    @Override
    public String toString() {
        return fullName + " (" + role.getDisplayName() + ")";
    }
}

// LSP: Ensure all subclasses of User maintain uniform permission contract
