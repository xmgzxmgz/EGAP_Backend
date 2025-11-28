package com.egap.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void health() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void tags() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    void tagsDistribution() throws Exception {
        mockMvc.perform(get("/api/tags/distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    void enterpriseBasicInfo() throws Exception {
        mockMvc.perform(get("/api/enterprise/basic-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    void modelingPressure() throws Exception {
        mockMvc.perform(get("/api/modeling/pressure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qps").isArray())
                .andExpect(jsonPath("$.time").isArray());
    }

    @Test
    void modelingTraining() throws Exception {
        mockMvc.perform(get("/api/modeling/training"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auc").isArray())
                .andExpect(jsonPath("$.loss").isArray());
    }

    @Test
    void search() throws Exception {
        mockMvc.perform(get("/api/search").param("q", "华"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());
    }
}