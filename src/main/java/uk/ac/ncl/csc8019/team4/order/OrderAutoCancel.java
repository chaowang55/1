package uk.ac.ncl.csc8019.team4.order;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs every minute and automatically cancels orders whose pick-up time has
 * passed by more than 15 minutes and are still in an active state.
 */
@Component
public class OrderAutoCancel {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancel.class);

    private static final List<OrderStatus> CANCELLABLE =
            List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS, OrderStatus.READY);

    private final OrderRepository orders;

    public OrderAutoCancel(OrderRepository orders) {
        this.orders = orders;
    }

    @Scheduled(fixedDelay = 60_000)
    public void autoCancelLateOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        List<Order> late = orders.findAllByStatusInOrderByPickupTimeDesc(CANCELLABLE).stream()
                .filter(o -> o.getPickupTime().isBefore(cutoff))
                .toList();

        if (!late.isEmpty()) {
            late.forEach(o -> o.cancel("No-show: customer did not collect within 15 minutes of pick-up time."));
            orders.saveAll(late);
            log.info("Auto-cancelled {} overdue order(s).", late.size());
        }
    }
}
