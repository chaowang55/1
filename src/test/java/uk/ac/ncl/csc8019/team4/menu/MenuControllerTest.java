package uk.ac.ncl.csc8019.team4.menu;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ncl.csc8019.team4.BaseIntegrationTest;

class MenuControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customerCanBrowseMenu() throws Exception {
        mockMvc.perform(get("/api/menu")).andExpect(status().isOk()).andExpect(jsonPath("$.length()", greaterThan(0)));
    }
}
