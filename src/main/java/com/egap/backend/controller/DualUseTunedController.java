package com.egap.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/dual_use_items_tuned")
public class DualUseTunedController {
    private final JdbcTemplate jdbc;

    public DualUseTunedController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/clone")
    public ResponseEntity<Map<String, Object>> cloneFromPrimary(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.GONE).body(new HashMap<>(Map.of("error", "deprecated; use /api/tag_relations")));
    }

    @PutMapping("/{itemId}/tag")
    public ResponseEntity<Map<String, Object>> addTag(@PathVariable("itemId") Integer itemId,
                                                      @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.GONE).body(new HashMap<>(Map.of("error", "deprecated; use /api/tag_relations")));
    }

    @PostMapping("/tag/by-name")
    public ResponseEntity<Map<String, Object>> addTagByEnterpriseName(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.GONE).body(new HashMap<>(Map.of("error", "deprecated; use /api/tag_relations")));
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam("modelId") Long modelId,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "20") int size,
                                    @RequestParam(value = "q", required = false) String q) {
        return new HashMap<>(Map.of("error", "deprecated; use /api/tag_relations"));
    }
}
