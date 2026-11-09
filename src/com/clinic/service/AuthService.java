package com.clinic.service;

import com.clinic.model.*;

import java.util.*;

/**
 * Concrete implementation of IAuthService.
 * Manages active user session and pre-configured role profiles.
 */
public class AuthService implements IAuthService {
    private User currentUser;
    private final Map<String, User> userDirectory = new HashMap<>();

    public AuthService() {
        // Pre-configure realistic demo user profiles for instant role switching
        ReceptionistUser rec = new ReceptionistUser("USR-01", "receptionist", "Emma Watson (Front Desk)", "Desk #1");
        DoctorUser doc1 = new DoctorUser("USR-02", "dr.sarah", "Dr. Sarah Jenkins (Cardiology)", "DOC-101");
        DoctorUser doc2 = new DoctorUser("USR-03", "dr.michael", "Dr. Michael Chen (Pediatrics)", "DOC-102");
        AdminUser admin = new AdminUser("USR-04", "admin", "Clinic Administrator");

        userDirectory.put("receptionist", rec);
        userDirectory.put("dr.sarah", doc1);
        userDirectory.put("dr.michael", doc2);
        userDirectory.put("admin", admin);

        // Default session on application start: Receptionist
        this.currentUser = rec;
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null) {
            throw new ValidationException("Username cannot be empty.");
        }
        User user = userDirectory.get(username.trim().toLowerCase());
        if (user != null) {
            this.currentUser = user;
            return user;
        }
        throw new ValidationException("Invalid username or credentials.");
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = Objects.requireNonNull(user, "User cannot be null");
    }

    @Override
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    @Override
    public Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : Role.RECEPTIONIST;
    }

    @Override
    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    @Override
    public List<User> getAvailableDemoUsers() {
        return new ArrayList<>(userDirectory.values());
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }
}
