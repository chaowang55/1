package uk.ac.ncl.csc8019.team4.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.ac.ncl.csc8019.team4.location.KioskLocation;
import uk.ac.ncl.csc8019.team4.user.User;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional link to a registered user account.
    // Guests (no account) will have this as null.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kiosk_location_id")
    private KioskLocation kioskLocation;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 254)
    private String customerEmail;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    // Guest order (no user account)
    public Order(String customerName, String customerEmail, LocalDateTime pickupTime, KioskLocation kioskLocation) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.pickupTime = pickupTime;
        this.kioskLocation = kioskLocation;
    }

    // Registered user order
    public Order(User user, LocalDateTime pickupTime, KioskLocation kioskLocation) {
        this.user = user;
        this.customerName = user.getFullName();
        this.customerEmail = user.getEmail();
        this.pickupTime = pickupTime;
        this.kioskLocation = kioskLocation;
    }

    // ── business helpers ──────────────────────────────────────────────────────

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        if (status != OrderStatus.CANCELLED) {
            this.cancellationReason = null;
        }
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    // ── getters ───────────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public KioskLocation getKioskLocation() {
        return kioskLocation;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public void setCancellationReason(String reason) {
        this.cancellationReason = reason;
    }
}
