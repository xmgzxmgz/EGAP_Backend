package com.egap.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DualUseItemsCacheControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void createCacheTableWithValuesMatrix() throws Exception {
        String json = "{" +
                "\"timestamp\":1733798400," +
                "\"columns\":[\"Number of Associated Enterprises\",\"Current Year ImportExport Amount 10k CNY\",\"Specialized Refined Unique New\",\"Registration Location\",\"Updated At\"]," +
                "\"values\":[" +
                "[0,123.45,true,\"Eastern Coastal\",\"2024-12-01T12:34:56Z\"]," +
                "[1,678.0,false,\"Western Region\",null]," +
                "[null,\"456.7\",\"false\",\"Central\",\"1733442000\"]" +
                "]" +
                "}";

        var mvcResult = mockMvc.perform(post("/api/dual_use_items_cache")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns", hasSize(5)))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.table", not(emptyString())))
                .andReturn();

        String table = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(mvcResult.getResponse().getContentAsString(), Map.class)
                .get("table").toString();

        Integer cnt = jdbc.queryForObject("select count(*) from dual_use_items_cache.\"" + table + "\"", Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(3, cnt);

        List<Map<String, Object>> cols = jdbc.queryForList(
                "select column_name, data_type from information_schema.columns where table_schema='dual_use_items_cache' and table_name=? order by ordinal_position",
                table);

        org.junit.jupiter.api.Assertions.assertEquals(5, cols.size());
        java.util.Set<String> types = new java.util.HashSet<>();
        for (Map<String, Object> c : cols) {
            types.add(String.valueOf(c.get("data_type")));
        }
        org.junit.jupiter.api.Assertions.assertTrue(types.contains("bigint"));
        org.junit.jupiter.api.Assertions.assertTrue(types.contains("double precision"));
        org.junit.jupiter.api.Assertions.assertTrue(types.contains("boolean"));
        org.junit.jupiter.api.Assertions.assertTrue(types.contains("text"));
        org.junit.jupiter.api.Assertions.assertTrue(types.contains("timestamp with time zone"));
    }
}
