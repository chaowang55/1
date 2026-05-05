package uk.ac.ncl.csc8019.team4.order;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ncl.csc8019.team4.BaseIntegrationTest;

class OrderControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customerCanPlaceValidOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(10, 30), 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Rebecca"))
                .andExpect(jsonPath("$.customerEmail").value("rebecca@test.com"));
    }

    @Test
    void customerCanViewOrderStatus() throws Exception {
        Integer id = createOrderAndReturnId();

        mockMvc.perform(get("/api/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void pastPickupTimeIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson("2020-01-01T10:30:00", 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidEmailIsRejected() throws Exception {
        String body = """
            {
              "customerName": "Rebecca",
              "customerEmail": "not-an-email",
              "pickupTime": "%s",
              "paymentMethod": "CASH",
              "items": [
                {
                  "menuItemId": 1,
                  "size": "REGULAR",
                  "quantity": 1
                }
              ]
            }
            """.formatted(futurePickupTime(10, 30));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyItemsIsRejected() throws Exception {
        String body = """
            {
              "customerName": "Rebecca",
              "customerEmail": "rebecca@test.com",
              "pickupTime": "%s",
              "paymentMethod": "CASH",
              "items": []
            }
            """.formatted(futurePickupTime(10, 30));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void quantityZeroIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(10, 30), 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void quantityOneIsAccepted() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(10, 30), 1)))
                .andExpect(status().isCreated());
    }

    @Test
    void quantityTwentyIsAccepted() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(10, 30), 20)))
                .andExpect(status().isCreated());
    }

    @Test
    void quantityTwentyOneIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(10, 30), 21)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownMenuItemIsRejected() throws Exception {
        String body = """
            {
              "customerName": "Rebecca",
              "customerEmail": "rebecca@test.com",
              "pickupTime": "%s",
              "paymentMethod": "CASH",
              "items": [
                {
                  "menuItemId": 99999,
                  "size": "REGULAR",
                  "quantity": 1
                }
              ]
            }
            """.formatted(futurePickupTime(10, 30));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void pickupExactlyOpeningTimeIsAccepted() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(6, 30), 1)))
                .andExpect(status().isCreated());
    }

    @Test
    void pickupOutsideOpeningHoursIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(23, 0), 1)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void staffCanUpdateOrderStatus() throws Exception {
        Integer id = createOrderAndReturnId();

        // PENDING -> ACCEPTED is the first allowed transition
        mockMvc.perform(patch("/api/orders/" + id + "/status?status=ACCEPTED")
                        .with(user("Admin").roles("STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void staffCanCancelOrderWithReason() throws Exception {
        Integer id = createOrderAndReturnId();

        String body = """
        {
          "reason": "Customer did not arrive"
        }
        """;

        mockMvc.perform(patch("/api/orders/" + id + "/cancel")
                        .with(user("Admin").roles("STAFF"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Customer did not arrive"));
    }

    private Integer createOrderAndReturnId() throws Exception {
        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson(futurePickupTime(10, 30), 1)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    private static String validOrderJson(String pickupTime, int quantity) {
        return """
            {
              "customerName": "Rebecca",
              "customerEmail": "rebecca@test.com",
              "pickupTime": "%s",
              "paymentMethod": "CASH",
              "items": [
                {
                  "menuItemId": 1,
                  "size": "REGULAR",
                  "quantity": %d
                }
              ]
            }
            """.formatted(pickupTime, quantity);
    }

    private static String futurePickupTime(int hour, int minute) {
        return LocalDateTime.now()
                .plusDays(1)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)
                .toString();
    }
}
