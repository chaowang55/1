package uk.ac.ncl.csc8019.team4.auth;

import uk.ac.ncl.csc8019.team4.user.User;
import uk.ac.ncl.csc8019.team4.user.UserRole;

public record AuthResponse(String token, Long id, String fullName, String email, UserRole role) {

    public static AuthResponse from(User user, String token) {
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
