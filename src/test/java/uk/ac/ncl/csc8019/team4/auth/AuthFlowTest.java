package uk.ac.ncl.csc8019.team4.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ncl.csc8019.team4.BaseIntegrationTest;

class AuthFlowTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerReturnsTokenAndRole() throws Exception {
        String text = """
            { "fullName": "Kareem", "email": "kareem@test.com", "password": "test1234"}
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(text))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void staffEndpointWithoutAuthBlocked() throws Exception {
        mockMvc.perform(get("/api/orders/dashboard")).andExpect(status().is4xxClientError());
    }

    @Test
    void staffEndpointWithCustomerRole() throws Exception {
        mockMvc.perform(get("/api/orders/dashboard").with(user("Kareem").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void staffEndpointWithStaffRole() throws Exception {
        mockMvc.perform(get("/api/orders/dashboard").with(user("Admin").roles("STAFF")))
                .andExpect(status().isOk());
    }
}
