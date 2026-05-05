package uk.ac.ncl.csc8019.team4.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ncl.csc8019.team4.auth.AuthService;
import uk.ac.ncl.csc8019.team4.location.KioskLocation;
import uk.ac.ncl.csc8019.team4.location.KioskLocationRepository;
import uk.ac.ncl.csc8019.team4.location.OperatingStatus;
import uk.ac.ncl.csc8019.team4.menu.MenuItem;
import uk.ac.ncl.csc8019.team4.menu.MenuItemRepository;
import uk.ac.ncl.csc8019.team4.payment.PaymentMethod;
import uk.ac.ncl.csc8019.team4.payment.PaymentService;
import uk.ac.ncl.csc8019.team4.payment.PaymentStatus;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final List<OrderStatus> ARCHIVED_STATUSES = List.of(OrderStatus.COLLECTED, OrderStatus.CANCELLED);

    private final OrderRepository orders;
    private final MenuItemRepository menuItems;
    private final OpeningHoursService openingHours;
    private final KioskLocationRepository locations;
    private final AuthService auth;
    private final PaymentService paymentService;

    public OrderController(
            OrderRepository orders,
            MenuItemRepository menuItems,
            OpeningHoursService openingHours,
            KioskLocationRepository locations,
            AuthService auth,
            PaymentService paymentService) {
        this.orders = orders;
        this.menuItems = menuItems;
        this.openingHours = openingHours;
        this.locations = locations;
        this.auth = auth;
        this.paymentService = paymentService;
    }

    // ── Customer endpoints ────────────────────────────────────────────────────

    /**
     * Place a new order.
     * POST /api/orders
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(
            @Valid @RequestBody PlaceOrderRequest req,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (!openingHours.isOpen(req.pickupTime())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "The kiosk is closed at the requested pick-up time.");
        }

        if (req.pickupTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Pick-up time must be in the future.");
        }

        KioskLocation location = req.kioskLocationId() == null
                ? locations
                        .findFirstByOperatingStatusOrderByIdAsc(OperatingStatus.OPEN)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "No active kiosk location is configured."))
                : locations
                        .findById(req.kioskLocationId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.UNPROCESSABLE_ENTITY, "Kiosk location not found: " + req.kioskLocationId()));

        Order order = auth.authenticate(authHeader)
                .map(user -> new Order(user, req.pickupTime(), location))
                .orElseGet(() -> {
                    // @NotBlank on the request fields forces registered users to also send name/email
                    // Instead we just check the guest path ourselves
                    if (req.customerName() == null
                            || req.customerName().isBlank()
                            || req.customerEmail() == null
                            || req.customerEmail().isBlank()) {
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Name and Email required.");
                    }
                    return new Order(req.customerName(), req.customerEmail(), req.pickupTime(), location);
                });

        for (PlaceOrderRequest.LineItemRequest line : req.items()) {
            MenuItem item = menuItems
                    .findById(line.menuItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY, "Menu item not found: " + line.menuItemId()));

            if (!item.isAvailable()) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "Menu item is currently unavailable: " + item.getName());
            }

            BigDecimal price = resolvePrice(item, line.size());
            order.addItem(new OrderItem(order, item, line.size(), line.quantity(), price, line.customisationNote()));

        }

        BigDecimal rawTotal = order.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        Optional<BigDecimal> cheapest = order.getItems().stream()
            .map(OrderItem::getUnitPrice)
            .min(BigDecimal::compareTo);
        BigDecimal finalTotal = rawTotal;
        if (order.getItems().stream().mapToInt(OrderItem::getQuantity).sum() >= 10) {
            finalTotal = rawTotal.subtract(cheapest.orElse(BigDecimal.ZERO));
        }
        order.setTotalCost(finalTotal);

        Order saved = orders.save(order);

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
     * Customer looks up their orders by email.
     * GET /api/orders?email=...
     */
    @GetMapping
    public List<Order> listByEmail(@RequestParam @Email @NotBlank String email) {
        return orders.findAllByCustomerEmail(email);
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
    public List<Order> dashboard() {
        return orders.findAllByStatusNotInOrderByPickupTimeAsc(ARCHIVED_STATUSES);
    }

    /**
     * Staff archive: collected and cancelled orders.
     * GET /api/orders/archive
     */
    @GetMapping("/archive")
    public List<Order> archive() {
        return orders.findAllByStatusInOrderByPickupTimeDesc(ARCHIVED_STATUSES);
    }

    /**
     * Staff updates the status of an order.
     * PATCH /api/orders/{id}/status
     */
    @PatchMapping("/{id}/status")
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
                            HttpStatus.UNPROCESSABLE_ENTITY, item.getName() + " does not have a regular size.");
                yield item.getRegularPrice();
            }
            case LARGE -> {
                if (item.getLargePrice() == null)
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY, item.getName() + " does not have a large size.");
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
