package uk.ac.ncl.csc8019.team4.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ncl.csc8019.team4.auth.Principal;
import uk.ac.ncl.csc8019.team4.user.User;
import uk.ac.ncl.csc8019.team4.user.UserRole;

@Service
public class JwtService {

    @Value("${app.jwt.secret:}")
    private String secret;

    @Value("${app.jwt.expiry-hours:24}")
    private long expiryHours;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret needs to be set");
        }
        if (secret.getBytes().length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
        }
        signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String issue(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(expiryHours))))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Principal> verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            return Optional.of(new Principal(userId, role));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
