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
            String tradeCo = Optional.ofNullable((String) rel.get("tradeCo")).orElse(null);
            if ((tradeCo == null || tradeCo.isBlank()) && rel.get("itemId") != null) {
                Object itemIdRaw = rel.get("itemId");
                tradeCo = String.valueOf(itemIdRaw);
            }
            String etpsName = Optional.ofNullable((String) rel.get("etpsName"))
                    .orElse(Optional.ofNullable((String) rel.get("enterpriseName")).orElse(null));
            Long modelId = rel.get("modelId") instanceof Number ? ((Number) rel.get("modelId")).longValue() : null;
            String modelName = Optional.ofNullable((String) rel.get("modelName")).orElse(null);
            String tag = Optional.ofNullable((String) rel.get("tag")).orElse(null);
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
            String tagCategory = Optional.ofNullable((String) rel.get("tagCategory"))
                    .orElseGet(() -> rel.get("itemId") != null ? "dual_use_items" : "manual");
            long epochMs = appliedAt.toEpochMilli();
            if (tradeCo == null || tradeCo.isBlank() || tag == null || tag.isBlank()) continue;
            java.sql.Timestamp ts = java.sql.Timestamp.from(appliedAt);
            affected += jdbc.update(
                    "insert into tag_relations(trade_co, etps_name, model_id, model_name, tag, applied_at, tag_status, project_status, tag_category) " +
                            "values(?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "on conflict (trade_co, model_id, tag) do update set etps_name=excluded.etps_name, model_id=excluded.model_id, model_name=excluded.model_name, tag=excluded.tag, applied_at=excluded.applied_at, tag_status=excluded.tag_status, project_status=excluded.project_status, tag_category=excluded.tag_category",
                    tradeCo, etpsName, modelId, modelName, tag, ts, tagStatus, projectStatus, tagCategory
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
            @RequestParam(value = "tradeCo", required = false) String tradeCo,
            @RequestParam(value = "trade_co", required = false) String trade_co,
            @RequestParam(value = "etpsName", required = false) String etpsName,
            @RequestParam(value = "enterpriseName", required = false) String enterpriseName,
            @RequestParam(value = "enterprise_name", required = false) String enterprise_name,
            @RequestParam(value = "enterpriseId", required = false) String enterpriseId,
            @RequestParam(value = "enterpriseid", required = false) String enterpriseid,
            @RequestParam(value = "enterpriseld", required = false) String enterpriseld,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "page_size", required = false) Integer page_size,
            @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "start", required = false) Long start,
            @RequestParam(value = "end", required = false) Long end,
            @RequestParam(value = "modelName", required = false) String modelName,
            @RequestParam(value = "model_name", required = false) String model_name
    ) {
        String code = tradeCo != null && !tradeCo.isBlank() ? tradeCo : trade_co;
        if (code == null || code.isBlank()) {
            String eid = enterpriseId != null && !enterpriseId.isBlank()
                    ? enterpriseId
                    : (enterpriseid != null && !enterpriseid.isBlank() ? enterpriseid : enterpriseld);
            if (eid != null && !eid.isBlank()) {
                code = eid;
            }
        }
        String ename = etpsName != null && !etpsName.isBlank()
                ? etpsName
                : (enterpriseName != null && !enterpriseName.isBlank() ? enterpriseName : enterprise_name);
        int p = page != null && page > 0 ? page : (pageNo != null && pageNo > 0 ? pageNo : 1);
        int s = size != null && size > 0 ? size : (page_size != null && page_size > 0 ? page_size : 20);
        s = Math.min(s, 200);
        int offset = (p - 1) * s;
        String where;
        List<Object> params = new ArrayList<>();
        if (code != null && !code.isBlank()) {
            where = " trade_co = ?";
            params.add(code);
        } else if (ename != null && !ename.isBlank()) {
            where = " lower(etps_name) = lower(?)";
            params.add(ename);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "enterprise required"));
        }
        Long st = startTime != null ? startTime : start;
        Long et = endTime != null ? endTime : end;
        if (st != null && et != null) {
            where += " and applied_at between ? and ?";
            params.add(java.sql.Timestamp.from(Instant.ofEpochMilli(st)));
            params.add(java.sql.Timestamp.from(Instant.ofEpochMilli(et)));
        } else if (st != null) {
            where += " and applied_at >= ?";
            params.add(java.sql.Timestamp.from(Instant.ofEpochMilli(st)));
        } else if (et != null) {
            where += " and applied_at <= ?";
            params.add(java.sql.Timestamp.from(Instant.ofEpochMilli(et)));
        }
        String mname = modelName != null && !modelName.isBlank() ? modelName : model_name;
        if (mname != null && !mname.isBlank()) {
            where += " and lower(model_name) = lower(?)";
            params.add(mname);
        }
        Long total = jdbc.queryForObject("select count(*) from tag_relations where" + where, Long.class, params.toArray());
        params.add(s);
        params.add(offset);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select tr.id, tr.trade_co, tr.etps_name, tr.model_id, tr.model_name, tr.tag, extract(epoch from tr.applied_at)*1000 as applied_at_ms, tr.tag_status, tr.project_status, tr.tag_category " +
                        "from tag_relations tr where" + where + " order by tr.applied_at desc limit ? offset ?",
                params.toArray()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("rows", rows);
        body.put("total", total == null ? 0 : total);
        return ResponseEntity.ok(body);
    }
}
