package uk.ac.ncl.csc8019.team4.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
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

    public User login(String email, String password) {
        User user = users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid details."));

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid details.");
        }

        return user;
    }

    public Optional<User> authenticate(String header) {
        if (header == null || !header.startsWith("Basic ")) {
            return Optional.empty();
        }

        String decoded;
        try {
            decoded =
                    new String(Base64.getDecoder().decode(header.substring("Basic ".length())), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid auth header.");
        }

        int colon = decoded.indexOf(":");
        if (colon < 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid auth header.");
        }

        return Optional.of(login(decoded.substring(0, colon), decoded.substring(colon + 1)));
    }
}
