package com.egap.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dual_use_items_tuned")
public class DualUseTunedController {
    private final JdbcTemplate jdbc;

    public DualUseTunedController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/clone")
    public ResponseEntity<Map<String, Object>> cloneFromPrimary(@RequestBody Map<String, Object> body) {
        Number modelId = (Number) body.get("modelId");
        if (modelId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(Map.of("error", "modelId required")));
        }
        try {
            jdbc.update("INSERT INTO dual_use_items_tuned (model_id, item_id) SELECT ?, item_id FROM dual_use_items ON CONFLICT (model_id, item_id) DO NOTHING", modelId.longValue());
            List<String> cols = jdbc.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name='dual_use_items'",
                    String.class
            );
            List<String> sets = new ArrayList<>();
            for (String c : cols) {
                if (!"item_id".equalsIgnoreCase(c)) {
                    sets.add("\"" + c + "\" = src.\"" + c + "\"");
                }
            }
            String sql = "UPDATE dual_use_items_tuned t SET " + String.join(", ", sets) + " FROM dual_use_items src WHERE t.item_id = src.item_id AND t.model_id = ?";
            jdbc.update(sql, modelId.longValue());
            String setModel = "UPDATE dual_use_items_tuned t SET model_name = tm.name, tuned_meta = jsonb_build_object('model_name', tm.name) || COALESCE(t.tuned_meta, '{}'::jsonb) FROM tuning_models tm WHERE tm.id = t.model_id AND t.model_id = ?";
            jdbc.update(setModel, modelId.longValue());
            return ResponseEntity.ok(new HashMap<>(Map.of("ok", Boolean.TRUE)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(Map.of("error", "clone failed")));
        }
    }

    @PutMapping("/{itemId}/tag")
    public ResponseEntity<Map<String, Object>> addTag(@PathVariable("itemId") Integer itemId,
                                                      @RequestBody Map<String, Object> body) {
        Number modelId = (Number) body.get("modelId");
        String key = Optional.ofNullable((String) body.get("key")).orElse("").trim();
        String value = Optional.ofNullable(body.get("value")).map(Object::toString).orElse("");
        if (modelId == null || key.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(Map.of("error", "modelId and key required")));
        }
        try {
            String sql = "UPDATE dual_use_items_tuned SET tuned_meta = jsonb_build_object(?, ?::text) || tuned_meta " +
                    "WHERE model_id = ? AND item_id = ?";
            int n = jdbc.update(sql, key, value, modelId.longValue(), itemId);
            if (n == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(Map.of("error", "not found")));
            }
            return ResponseEntity.ok(new HashMap<>(Map.of("ok", Boolean.TRUE)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(Map.of("error", "update failed")));
        }
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam("modelId") Long modelId,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "20") int size,
                                    @RequestParam(value = "q", required = false) String q) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> present = jdbc.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name='dual_use_items_tuned'",
                    String.class
            );
            List<String> selectParts = new ArrayList<>();
            selectParts.add("item_id");
            if (present.contains("tuned_meta")) selectParts.add("tuned_meta");
            if (present.contains("model_name")) selectParts.add("model_name");
            String[] specs = new String[]{
                    "Consignee Enterprise",
                    "Registration Location",
                    "Industry Category"
            };
            for (String col : specs) {
                if (present.contains(col)) selectParts.add("\"" + col + "\"");
            }
            String where = " WHERE model_id = ?";
            List<Object> params = new ArrayList<>();
            params.add(modelId);
            List<String> likes = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                if (present.contains("Consignee Enterprise")) { likes.add("lower(\"Consignee Enterprise\") LIKE ?"); params.add(like); }
                if (present.contains("Registration Location")) { likes.add("lower(\"Registration Location\") LIKE ?"); params.add(like); }
                if (present.contains("Industry Category")) { likes.add("lower(\"Industry Category\") LIKE ?"); params.add(like); }
                if (!likes.isEmpty()) where += " AND (" + String.join(" OR ", likes) + ")";
            }
            String selectSql = "SELECT " + String.join(", ", selectParts) + " FROM dual_use_items_tuned" + where + " ORDER BY item_id LIMIT ? OFFSET ?";
            String countSql = "SELECT count(*) FROM dual_use_items_tuned" + where;
            long total = jdbc.queryForObject(countSql, params.toArray(), Long.class);
            params.add(size);
            params.add(page * size);
            List<Map<String, Object>> rows = jdbc.queryForList(selectSql, params.toArray());
            result.put("rows", rows);
            result.put("total", total);
        } catch (Exception e) {
            result.put("rows", List.of());
            result.put("total", 0);
        }
        return result;
    }
}
