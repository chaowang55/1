package uk.ac.ncl.csc8019.team4.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import uk.ac.ncl.csc8019.team4.menu.MenuItem;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ItemSize size;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 5, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "customisation_note", length = 255)
    private String customisationNote;

    protected OrderItem() {}

    public OrderItem(Order order, MenuItem menuItem, ItemSize size, int quantity, BigDecimal unitPrice) {
        this(order, menuItem, size, quantity, unitPrice, null);
    }

    public OrderItem(
            Order order,
            MenuItem menuItem,
            ItemSize size,
            int quantity,
            BigDecimal unitPrice,
            String customisationNote) {
        this.order = order;
        this.menuItem = menuItem;
        this.size = size;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.customisationNote = customisationNote;
    }

    public Long getId() {
        return id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public ItemSize getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getCustomisationNote() {
        return this.customisationNote;
    }
}
