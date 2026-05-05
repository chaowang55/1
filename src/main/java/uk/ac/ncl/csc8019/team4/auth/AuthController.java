package uk.ac.ncl.csc8019.team4.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.ac.ncl.csc8019.team4.auth.jwt.JwtService;
import uk.ac.ncl.csc8019.team4.user.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final JwtService jwt;

    public AuthController(AuthService auth, JwtService jwt) {
        this.auth = auth;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = auth.register(req.fullName(), req.email(), req.password());
        return AuthResponse.from(user, jwt.issue(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = auth.login(req.email(), req.password(), req.expectedRole());
        return AuthResponse.from(user, jwt.issue(user));
    }
}
