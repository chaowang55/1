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

    // ✅ 修复：添加缺失的 cupCount 字段
    @Column(name = "cup_count", nullable = false)
    private Integer cupCount = 0;

    protected User() {}

    public User(String fullName, String email, String passwordHash, UserRole userRole) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = userRole;
        this.cupCount = 0; // 初始化
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

    // ✅ 修复：添加缺失的 getCupCount 方法
    public Integer getCupCount() {
        return cupCount;
    }

    // 可选：添加 setter（如果业务需要修改杯子数量）
    public void setCupCount(Integer cupCount) {
        this.cupCount = cupCount;
    }
}
