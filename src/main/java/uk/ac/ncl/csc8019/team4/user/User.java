package uk.ac.ncl.csc8019.team4.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole role = UserRole.CUSTOMER;

    @Column(name = "cup_count", nullable = false)
    private int cupCount = 0;

    protected User() {}

    public User(String fullName, String email, String passwordHash, UserRole userRole) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = userRole;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public int getCupCount() {
        return cupCount;
    }

    public void setCupCount(int cupCount) {
        this.cupCount = cupCount;
    }
}
