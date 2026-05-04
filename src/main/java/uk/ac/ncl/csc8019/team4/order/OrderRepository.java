package uk.ac.ncl.csc8019.team4.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Active orders shown on the staff dashboard (everything except archived terminal states). */
    List<Order> findAllByStatusNotInOrderByPickupTimeAsc(List<OrderStatus> statuses);

    /** Archived orders: collected or cancelled. */
    List<Order> findAllByStatusInOrderByPickupTimeDesc(List<OrderStatus> statuses);

    /** Customer-facing: look up orders by email. */
    List<Order> findAllByCustomerEmail(String email);
}
