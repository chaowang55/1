package uk.ac.ncl.csc8019.team4.auth;

import uk.ac.ncl.csc8019.team4.user.UserRole;

public record Principal(Long userId, UserRole role) {}
