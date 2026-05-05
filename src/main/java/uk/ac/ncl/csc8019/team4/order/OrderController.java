package uk.ac.ncl.csc8019.team4.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ncl.csc8019.team4.auth.Principal;
import uk.ac.ncl.csc8019.team4.auth.StaffLock;
import uk.ac.ncl.csc8019.team4.location.KioskLocation;
import uk.ac.ncl.csc8019.team4.location.KioskLocationRepository;
import uk.ac.ncl.csc8019.team4.location.OperatingStatus;
import uk.ac.ncl.csc8019.team4.menu.MenuItem;
import uk.ac.ncl.csc8019.team4.menu.MenuItemRepository;
import uk.ac.ncl.csc8019.team4.payment.PaymentMethod;
import uk.ac.ncl.csc8019.team4.payment.PaymentService;
import uk.ac.ncl.csc8019.team4.payment.PaymentStatus;
import uk.ac.ncl.csc8019.team4.user.User;
import uk.ac.ncl.csc8019.team4.user.UserRepository;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final List<OrderStatus> ARCHIVED_STATUSES = List.of(OrderStatus.COLLECTED, OrderStatus.CANCELLED);

    private final OrderRepository orders;
    private final MenuItemRepository menuItems;
    private final OpeningHoursService openingHours;
    private final KioskLocationRepository locations;
    private final UserRepository users;
    private final PaymentService paymentService;

    public OrderController(
            OrderRepository orders,
            MenuItemRepository menuItems,
            OpeningHoursService openingHours,
            KioskLocationRepository locations,
            UserRepository users,
            PaymentService paymentService) {
        this.orders = orders;
        this.menuItems = menuItems;
        this.openingHours = openingHours;
        this.locations = locations;
        this.users = users;
        this.paymentService = paymentService;
    }

    // ── Customer endpoints ────────────────────────────────────────────────────

    /**
     * Place a new order.
     * POST /api/orders
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Order place(@Valid @RequestBody PlaceOrderRequest req, @AuthenticationPrincipal Principal me) {

        if (!openingHours.isOpen(req.pickupTime())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "The kiosk is closed at the requested pick-up time.");
        }

        if (req.pickupTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Pick-up time must be in the future.");
        }

        KioskLocation location = req.kioskLocationId() == null
                ? locations
                        .findFirstByOperatingStatusOrderByIdAsc(OperatingStatus.OPEN)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "No active kiosk location is configured."))
                : locations
                        .findById(req.kioskLocationId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.UNPROCESSABLE_CONTENT,
                                "Kiosk location not found: " + req.kioskLocationId()));

        Order order;
        User user = null;
        if (me != null) {
            user = users.findById(me.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            order = new Order(user, req.pickupTime(), location);
        } else {
            // Guest Order
            if (req.customerName() == null
                    || req.customerName().isBlank()
                    || req.customerEmail() == null
                    || req.customerEmail().isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Name and Email required.");
            }
            order = new Order(req.customerName(), req.customerEmail(), req.pickupTime(), location);
        }

        for (PlaceOrderRequest.LineItemRequest line : req.items()) {
            MenuItem item = menuItems
                    .findById(line.menuItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_CONTENT, "Menu item not found: " + line.menuItemId()));

            if (!item.isAvailable()) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT, "Menu item is currently unavailable: " + item.getName());
            }

            BigDecimal price = resolvePrice(item, line.size());
            order.addItem(new OrderItem(order, item, line.size(), line.quantity(), price, line.customisationNote()));
        }

        BigDecimal totalCost =
                order.getItems().stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        int orderCups =
                order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        boolean freeCup = (me != null) ? user.getCupCount() + orderCups >= 10 : orderCups >= 10;

        if (freeCup) {
            BigDecimal cheapest = order.getItems().stream()
                    .map(OrderItem::getUnitPrice)
                    .min(BigDecimal::compareTo)
                    .orElseThrow();
            order.setDiscountAmount(cheapest);
            totalCost = totalCost.subtract(cheapest);
        }
        order.setTotalCost(totalCost);

        Order saved = orders.save(order);

        if (me != null) {
            int newCount = user.getCupCount() + orderCups - (freeCup ? 10 : 0);
            user.setCupCount(newCount);
            users.save(user);
        }
        if (req.paymentMethod() == PaymentMethod.CARD) {
            if (paymentService.chargeCard(saved).getStatus() == PaymentStatus.FAILED) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Card payment declined.");
            }
        } else {
            paymentService.createPending(saved, PaymentMethod.CASH);
        }
        return saved;
    }

    /**
     * Customer looks up their orders
     * GET /api/orders
     */
    @GetMapping
    public List<Order> listMyOrders(@AuthenticationPrincipal Principal me) {
        if (me == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required.");
        }
        User user = users.findById(me.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found."));
        return orders.findAllByCustomerEmail(user.getEmail());
    }

    /**
     * Get a single order (customer receipt / live status polling).
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return findOrThrow(id);
    }

    // ── Staff endpoints ───────────────────────────────────────────────────────

    /**
     * Staff dashboard: all active (non-archived) orders.
     * GET /api/orders/dashboard
     */
    @GetMapping("/dashboard")
    @StaffLock
    public List<Order> dashboard() {
        return orders.findAllByStatusNotInOrderByPickupTimeAsc(ARCHIVED_STATUSES);
    }

    /**
     * Staff archive: collected and cancelled orders.
     * GET /api/orders/archive
     */
    @GetMapping("/archive")
    @StaffLock
    public List<Order> archive() {
        return orders.findAllByStatusInOrderByPickupTimeDesc(ARCHIVED_STATUSES);
    }

    /**
     * Staff updates the status of an order.
     * PATCH /api/orders/{id}/status
     */
    @PatchMapping("/{id}/status")
    @StaffLock
    public Order updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order order = findOrThrow(id);
        if (!order.getStatus().canTransitionTo(status)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Can't transition from " + order.getStatus() + " to " + status);
        }
        order.setStatus(status);
        return orders.save(order);
    }

    /**
     * Staff cancels an order (convenience shortcut).
     * PATCH /api/orders/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    @StaffLock
    public Order cancel(@PathVariable Long id, @RequestBody(required = false) CancelRequest req) {
        Order order = findOrThrow(id);
        if (order.getStatus() == OrderStatus.COLLECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel a collected order.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        if (req != null && req.reason() != null) {
            order.setCancellationReason(req.reason());
        }
        return orders.save(order);
    }

    public record CancelRequest(String reason) {}

    // ── helpers ───────────────────────────────────────────────────────────────

    private Order findOrThrow(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    private BigDecimal resolvePrice(MenuItem item, ItemSize size) {
        return switch (size) {
            case REGULAR -> {
                if (item.getRegularPrice() == null)
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_CONTENT, item.getName() + " does not have a regular size.");
                yield item.getRegularPrice();
            }
            case LARGE -> {
                if (item.getLargePrice() == null)
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_CONTENT, item.getName() + " does not have a large size.");
                yield item.getLargePrice();
            }
        };
    }

    // ── request / response records ────────────────────────────────────────────

    public record PlaceOrderRequest(
            @Size(max = 120) String customerName,
            @Email @Size(max = 254) String customerEmail,
            @NotNull @Future LocalDateTime pickupTime,
            Long kioskLocationId,
            @NotNull PaymentMethod paymentMethod,
            @Valid @NotEmpty List<LineItemRequest> items) {

        public record LineItemRequest(
                @NotNull Long menuItemId,
                @NotNull ItemSize size,
                @Min(1) @Max(20) int quantity,
                @Size(max = 255) String customisationNote) {} // add @Max(20)
    }

    public record CancelOrderRequest(@Size(max = 255) String reason) {}
}
