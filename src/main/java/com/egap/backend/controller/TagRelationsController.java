package com.egap.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/tag_relations")
public class TagRelationsController {
    private final JdbcTemplate jdbc;

    public TagRelationsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/bulk")
    public Map<String, Object> bulkCreate(@RequestBody Object payload) {
        List<Map<String, Object>> relations = new ArrayList<>();
        if (payload instanceof Map) {
            Object r = ((Map<?, ?>) payload).get("relations");
            if (r instanceof List) relations = (List<Map<String, Object>>) r;
        } else if (payload instanceof List) {
            relations = (List<Map<String, Object>>) payload;
        }
        int affected = 0;
        for (Map<String, Object> rel : relations) {
            Long itemId = rel.get("itemId") instanceof Number ? ((Number) rel.get("itemId")).longValue() : null;
            String enterpriseName = Optional.ofNullable((String) rel.get("enterpriseName")).orElse(null);
            Long modelId = rel.get("modelId") instanceof Number ? ((Number) rel.get("modelId")).longValue() : null;
            String modelName = Optional.ofNullable((String) rel.get("modelName")).orElse(null);
            String tag = Optional.ofNullable((String) rel.get("tag")).orElse(null);
            Long tagId = rel.get("tagId") instanceof Number ? ((Number) rel.get("tagId")).longValue() : null;
            Instant appliedAt;
            Object appliedAtRaw = rel.get("appliedAt");
            if (appliedAtRaw instanceof Instant) {
                appliedAt = (Instant) appliedAtRaw;
            } else if (appliedAtRaw instanceof String) {
                try {
                    appliedAt = Instant.parse((String) appliedAtRaw);
                } catch (Exception e) {
                    appliedAt = Instant.now();
                }
            } else if (appliedAtRaw instanceof Number) {
                try {
                    long epoch = ((Number) appliedAtRaw).longValue();
                    // 兼容毫秒与秒
                    appliedAt = Instant.ofEpochMilli(epoch > 9999999999L ? epoch : epoch * 1000);
                } catch (Exception e) {
                    appliedAt = Instant.now();
                }
            } else {
                appliedAt = Instant.now();
            }
            String tagStatus = Optional.ofNullable((String) rel.get("tagStatus")).orElse("active");
            String projectStatus = Optional.ofNullable((String) rel.get("projectStatus")).orElse("archived");
            if (itemId == null && enterpriseName != null && !enterpriseName.isBlank()) {
                try {
                    itemId = jdbc.queryForObject("select id from enterprise_info where lower(name)=lower(?) limit 1", Long.class, enterpriseName);
                } catch (Exception ignored) {}
            }
            if (tagId == null && tag != null && !tag.isBlank()) {
                try {
                    tagId = jdbc.queryForObject("select id from tags where name = ? limit 1", Long.class, tag);
                } catch (Exception ignored) {}
            }
            long epochMs = appliedAt.toEpochMilli();
            if (tagId == null && tag != null && !tag.isBlank()) {
                try {
                    Long inserted = jdbc.queryForObject(
                            "insert into tags(name, created_at_ms) values(?, ?) on conflict(name) do nothing returning id",
                            Long.class, tag, epochMs
                    );
                    if (inserted != null) tagId = inserted;
                } catch (Exception ignored) {}
                if (tagId == null) {
                    try {
                        tagId = jdbc.queryForObject("select id from tags where name = ? limit 1", Long.class, tag);
                    } catch (Exception ignored) {}
                }
            }
            if (itemId == null || tagId == null) continue;
            java.sql.Timestamp ts = java.sql.Timestamp.from(appliedAt);
            affected += jdbc.update(
                    "insert into tag_relations(enterprise_id, item_id, enterprise_name, model_id, model_name, tag_id, tag, applied_at, applied_at_ms, tag_status, project_status) " +
                            "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "on conflict (enterprise_id, tag_id, applied_at_ms) do update set enterprise_name=excluded.enterprise_name, model_id=excluded.model_id, model_name=excluded.model_name, tag=excluded.tag, applied_at=excluded.applied_at, tag_status=excluded.tag_status, project_status=excluded.project_status",
                    itemId, itemId, enterpriseName, modelId, modelName, tagId, tag, ts, epochMs, tagStatus, projectStatus
            );
        }
        return Map.of("ok", Boolean.TRUE, "count", affected);
    }

    @PutMapping("/status")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> body) {
        Long modelId = body.get("modelId") instanceof Number ? ((Number) body.get("modelId")).longValue() : null;
        String modelName = Optional.ofNullable((String) body.get("modelName")).orElse(null);
        List<?> tags = Optional.ofNullable((List<?>) body.get("tags")).orElse(Collections.emptyList());
        String tagStatus = Optional.ofNullable((String) body.get("tagStatus")).orElse(null);
        String projectStatus = Optional.ofNullable((String) body.get("projectStatus")).orElse(null);
        List<Object> whereParams = new ArrayList<>();
        String where = "";
        if (modelId != null) { where += " model_id = ?"; whereParams.add(modelId); }
        else if (modelName != null && !modelName.isBlank()) { where += " lower(model_name) = lower(?)"; whereParams.add(modelName); }
        else { return Map.of("ok", Boolean.FALSE, "error", "model required"); }
        if (!tags.isEmpty()) {
            where += " and tag = any(?)";
            whereParams.add(tags.toArray(new String[0]));
        }
        List<String> sets = new ArrayList<>();
        List<Object> setParams = new ArrayList<>();
        if (tagStatus != null) { sets.add("tag_status = ?"); setParams.add(tagStatus); }
        if (projectStatus != null) { sets.add("project_status = ?"); setParams.add(projectStatus); }
        sets.add("applied_at = now()");
        if (sets.isEmpty()) return Map.of("ok", Boolean.FALSE, "error", "no status provided");
        List<Object> allParams = new ArrayList<>(setParams);
        allParams.addAll(whereParams);
        int updated = jdbc.update("update tag_relations set " + String.join(", ", sets) + " where" + where, allParams.toArray());
        return Map.of("ok", Boolean.TRUE, "count", updated);
    }

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(value = "enterpriseId", required = false) Long enterpriseId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "modelName", required = false) String modelName
    ) {
        if (enterpriseId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "enterpriseId required"));
        }
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 200);
        int offset = (p - 1) * s;
        String where = " enterprise_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(enterpriseId);
        if (startTime != null && endTime != null) {
            where += " and applied_at_ms between ? and ?";
            params.add(startTime);
            params.add(endTime);
        } else if (startTime != null) {
            where += " and applied_at_ms >= ?";
            params.add(startTime);
        } else if (endTime != null) {
            where += " and applied_at_ms <= ?";
            params.add(endTime);
        }
        if (modelName != null && !modelName.isBlank()) {
            where += " and lower(model_name) = lower(?)";
            params.add(modelName);
        }
        Long total = jdbc.queryForObject("select count(*) from tag_relations where" + where, Long.class, params.toArray());
        params.add(s);
        params.add(offset);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select tr.id, tr.enterprise_id, tr.enterprise_name, tr.model_id, tr.model_name, coalesce(t.name, tr.tag) as tag, tr.applied_at_ms, tr.tag_status, tr.project_status " +
                        "from tag_relations tr left join tags t on tr.tag_id = t.id where" + where + " order by tr.applied_at_ms desc limit ? offset ?",
                params.toArray()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("rows", rows);
        body.put("total", total == null ? 0 : total);
        return ResponseEntity.ok(body);
    }
}
