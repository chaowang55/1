package uk.ac.ncl.csc8019.team4.payment;

import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/orders/{orderId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public Payment get(@PathVariable Long orderId) {
        return paymentService
                .findByOrderId(orderId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: Order#" + orderId));
    }

    @PostMapping("/mark-paid")
    public Payment markPaid(@PathVariable Long orderId, @RequestBody MarkPaidRequest req) {
        return paymentService.markPaidManually(orderId, req.reference());
    }

    public record MarkPaidRequest(@Size(max = 64) String reference) {}
}
