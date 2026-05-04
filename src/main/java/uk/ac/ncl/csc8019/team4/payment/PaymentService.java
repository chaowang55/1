package uk.ac.ncl.csc8019.team4.payment;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ncl.csc8019.team4.order.Order;
import uk.ac.ncl.csc8019.team4.payment.horsepay.HorsePayClient;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final HorsePayClient horsePay;

    public PaymentService(PaymentRepository payments, HorsePayClient horsepay) {
        this.payments = payments;
        this.horsePay = horsepay;
    }

    public Payment createPending(Order order, PaymentMethod method) {
        return payments.save(new Payment(order, method));
    }

    public Payment chargeCard(Order order) {
        Payment payment = createPending(order, PaymentMethod.CARD);
        HorsePayClient.ChargeResult result = horsePay.charge(order.getCustomerEmail(), order.getTotalCost());
        if (result.success()) {
            payment.markPaid(result.transactionReference());
        } else {
            payment.markFailed();
        }
        return payments.save(payment);
    }

    public Payment markPaidManually(Long orderId, String reference) {
        Payment payment = payments.findByOrderId(orderId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: Order#" + orderId));
        payment.markPaid(reference);
        return payments.save(payment);
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        return payments.findByOrderId(orderId);
    }
}
