package uk.ac.ncl.csc8019.team4.auth;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ncl.csc8019.team4.user.User;
import uk.ac.ncl.csc8019.team4.user.UserRepository;
import uk.ac.ncl.csc8019.team4.user.UserRole;

@Service
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public User register(String fullName, String email, String password) {
        if (users.existsByEmail(email.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return users.save(new User(fullName, email.toLowerCase(), hash, UserRole.CUSTOMER));
    }

    public User login(String email, String password, UserRole expectedRole) {
        User user = users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid details."));

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid details.");
        }

        if (expectedRole != null && user.getRole() != expectedRole) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account is not a " + expectedRole.name().toLowerCase() + " account.");
        }

        return user;
    }
}
