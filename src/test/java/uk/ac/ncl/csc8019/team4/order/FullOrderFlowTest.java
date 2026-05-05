package uk.ac.ncl.csc8019.team4.order;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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

class FullOrderFlowTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullOrderLifecycleWorksFromCustomerOrderToArchive() throws Exception {
        Integer orderId = createOrder();

        mockMvc.perform(get("/api/orders/dashboard").with(user("Admin").roles("STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        updateStatus(orderId, "ACCEPTED");
        updateStatus(orderId, "IN_PROGRESS");
        updateStatus(orderId, "READY"); // Bug 2 fix: was READY_FOR_COLLECTION
        updateStatus(orderId, "COLLECTED");

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk()) // Bug 3 fix: was isCreated()
                .andExpect(jsonPath("$.status").value("COLLECTED"));

        mockMvc.perform(get("/api/orders/archive").with(user("Admin").roles("STAFF")))
                .andExpect(status().isOk()) // Bug 4 fix: was isCreated()
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    private Integer createOrder() throws Exception {
        String pickupTime = LocalDateTime.now()
                .plusDays(1)
                .withHour(10)
                .withMinute(30)
                .withSecond(0)
                .withNano(0)
                .toString();

        String body = """
            {
              "customerName": "Rebecca",
              "customerEmail": "rebecca@test.com",
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
            """.formatted(pickupTime);

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    private void updateStatus(Integer orderId, String status) throws Exception {
        // Bug 1 fix: pass status as query param, not request body
        mockMvc.perform(patch("/api/orders/" + orderId + "/status?status=" + status)
                        .with(user("Admin").roles("STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status));
    }
}
