package com.egap.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/tag-relations-columns")
    public Map<String, Object> tagRelationsColumns() {
        Map<String, Object> r = new HashMap<>();
        try {
            var rows = jdbcTemplate.queryForList(
                    "select table_schema, table_name, column_name, data_type " +
                            "from information_schema.columns " +
                            "where table_name = 'tag_relations' " +
                            "order by ordinal_position"
            );
            r.put("ok", true);
            r.put("columns", rows);
        } catch (Exception e) {
            r.put("ok", false);
            r.put("error", e.getClass().getSimpleName());
        }
        return r;
    }

    @PostMapping("/migrate-tag-category")
    public Map<String, Object> migrateTagCategory() {
        Map<String, Object> r = new HashMap<>();
        try {
            jdbcTemplate.execute(
                    "do $$\n" +
                            "begin\n" +
                            "  begin\n" +
                            "    alter table tag_relations add column tag_category text;\n" +
                            "  exception when duplicate_column then null; end;\n" +
                            "end $$;"
            );
            r.put("ok", true);
        } catch (Exception e) {
            r.put("ok", false);
            r.put("error", e.getMessage());
        }
        return r;
    }

    @PostMapping("/tag-relations-backup-reset")
    public Map<String, Object> backupAndResetTagRelations() {
        Map<String, Object> r = new HashMap<>();
        try {
            String backupName = jdbcTemplate.queryForObject(
                    "select 'tag_relations_backup_' || to_char(now(), 'YYYYMMDDHH24MISS')",
                    String.class
            );
            jdbcTemplate.execute("create table " + backupName + " as table tag_relations");
            jdbcTemplate.execute("truncate table tag_relations restart identity");
            r.put("ok", true);
            r.put("backupTable", backupName);
        } catch (Exception e) {
            r.put("ok", false);
            r.put("error", e.getMessage());
        }
        return r;
    }
}
