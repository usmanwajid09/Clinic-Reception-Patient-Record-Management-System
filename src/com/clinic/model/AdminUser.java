package com.clinic.model;

/**
 * Concrete Administrator user model.
 * Adheres to LSP.
 */
public class AdminUser extends User {

    public AdminUser(String userId, String username, String fullName) {
        super(userId, username, fullName, Role.ADMINISTRATOR);
    }

    @Override
    public boolean hasAdministrativeAccess() {
        return true;
    }

    @Override
    public boolean canConductConsultations() {
        return false;
    }

    @Override
    public boolean canManageFrontDesk() {
        return true;
    }
}
