package uk.ac.ncl.csc8019.team4.payment.horsepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class HorsePayClient {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final RestTemplate restTemplate;

    @Value("${horsepay.api-url:http://homepages.cs.ncl.ac.uk/daniel.nesbitt/CSC8019/HorsePay/HorsePay.php}")
    private String apiUrl;

    @Value("${horsepay.store-id:Team04}")
    private String storeId;

    public HorsePayClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ChargeResult charge(String customerId, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        ChargeRequest body = new ChargeRequest(
                storeId, customerId, DATE_FMT.format(now), TIME_FMT.format(now), "GMT", amount.doubleValue(), "GBP");

        try {
            HorsePayResponse resp = restTemplate.postForObject(apiUrl, body, HorsePayResponse.class);
            if (resp == null || resp.outcome() == null) {
                return ChargeResult.fail("Empty response");
            }
            Boolean status = resp.outcome().status();
            if (status != null && status) {
                return ChargeResult.success("HP-" + customerId + "-" + now.toEpochSecond(ZoneOffset.UTC));
            }
            return ChargeResult.fail(resp.outcome().reason());
        } catch (RestClientException e) {
            return ChargeResult.fail(e.getMessage());
        }
    }

    record ChargeRequest(
            String storeID,
            String customerID,
            String date,
            String time,
            String timeZone,
            Double transactionAmount,
            String currencyCode) {}

    record HorsePayResponse(
            String storeID,
            String customerID,
            Double transactionAmount,
            @JsonProperty("paymetSuccess") HorsePayOutcome outcome) {}

    record HorsePayOutcome(@JsonProperty("Status") Boolean status, String reason) {}

    public record ChargeResult(boolean success, String transactionReference, String failReason) {
        public static ChargeResult success(String ref) {
            return new ChargeResult(true, ref, null);
        }

        public static ChargeResult fail(String failReason) {
            return new ChargeResult(false, null, failReason);
        }
    }
}
