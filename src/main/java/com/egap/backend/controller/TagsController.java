package com.egap.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagsController {
    private final JdbcTemplate jdbc;

    public TagsController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> delete(@PathVariable("id") Long id) {
        int affected = jdbc.update("delete from tags where id = ?", id);
        if (affected == 0) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "not found"));
        return ResponseEntity.ok(Map.of("ok", true, "deleted", affected));
    }
}
