package com.clinic.service;

import com.clinic.model.Role;
import com.clinic.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for User Authentication & Session Management.
 */
public interface IAuthService {
    User authenticate(String username, String password);
    void setCurrentUser(User user);
    Optional<User> getCurrentUser();
    Role getCurrentRole();
    boolean hasRole(Role role);
    List<User> getAvailableDemoUsers();
    void logout();
}

