package com.egap.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PolicyController {
    @GetMapping("/policies")
    public List<Map<String, Object>> getPolicies() {
        return List.of(
                Map.of("id", 1, "title", "出口管制清单更新", "date", "2025-11-01"),
                Map.of("id", 2, "title", "两用物项合规提醒", "date", "2025-11-15")
        );
    }
}
