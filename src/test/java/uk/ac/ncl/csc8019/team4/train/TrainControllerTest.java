package uk.ac.ncl.csc8019.team4.train;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ncl.csc8019.team4.BaseIntegrationTest;

class TrainControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void trainArrivalsEndpointExistsAndDoesNotCrashWithoutToken() throws Exception {
        mockMvc.perform(get("/api/trains/arrivals?count=5")).andExpect(status().isOk());
    }

    @Test
    void trainArrivalsRejectsCountBelowMinimum() throws Exception {
        mockMvc.perform(get("/api/trains/arrivals?count=0")).andExpect(status().isBadRequest());
    }

    @Test
    void trainArrivalsRejectsCountAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/trains/arrivals?count=21")).andExpect(status().isBadRequest());
    }
}
