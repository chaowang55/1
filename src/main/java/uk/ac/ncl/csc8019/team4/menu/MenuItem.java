package uk.ac.ncl.csc8019.team4.menu;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "regular_price", precision = 5, scale = 2)
    private BigDecimal regularPrice;

    @Column(name = "large_price", precision = 5, scale = 2)
    private BigDecimal largePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category = Category.HOT_DRINK;

    @Column(nullable = false)
    private boolean available = true;

    protected MenuItem() {}

    public MenuItem(String name, BigDecimal regularPrice, BigDecimal largePrice, Category category) {
        this.name = name;
        this.regularPrice = regularPrice;
        this.largePrice = largePrice;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getRegularPrice() {
        return regularPrice;
    }

    public BigDecimal getLargePrice() {
        return largePrice;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRegularPrice(BigDecimal regularPrice) {
        this.regularPrice = regularPrice;
    }

    public void setLargePrice(BigDecimal largePrice) {
        this.largePrice = largePrice;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
