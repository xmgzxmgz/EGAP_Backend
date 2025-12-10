package com.egap.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DualItemTagsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndListArchived() throws Exception {
        String payload = "{\n" +
                "  \"name\": \"模型A\",\n" +
                "  \"creator\": \"测试用户\",\n" +
                "  \"createdAt\": 1733820000000,\n" +
                "  \"status\": \"archived\",\n" +
                "  \"meta\": { \"note\": \"备注内容\" }\n" +
                "}";
        mockMvc.perform(post("/api/dual_item_tags")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("模型A"))
                .andExpect(jsonPath("$.status").value("archived"))
                .andExpect(jsonPath("$.remark").value("备注内容"));

        mockMvc.perform(get("/api/dual_item_tags").param("status", "archived"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());
    }
}

