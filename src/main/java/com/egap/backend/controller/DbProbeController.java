package com.egap.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/db")
public class DbProbeController {
    private final JdbcTemplate jdbcTemplate;

    public DbProbeController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> r = new HashMap<>();
        try {
            String version = jdbcTemplate.queryForObject("select version()", String.class);
            r.put("ok", true);
            r.put("version", version);
        } catch (Exception e) {
            r.put("ok", false);
            r.put("error", e.getClass().getSimpleName());
        }
        return r;
    }

    @GetMapping("/count")
    public Map<String, Object> count() {
        Map<String, Object> r = new HashMap<>();
        try {
            Long c = jdbcTemplate.queryForObject("select count(*) from dual_use_items", Long.class);
            r.put("table", "dual_use_items");
            r.put("count", c);
        } catch (Exception e) {
            r.put("table", "dual_use_items");
            r.put("count", 0);
            r.put("error", e.getClass().getSimpleName());
        }
        return r;
    }
}
