package com.egap.backend.controller;

import com.egap.backend.model.TuningModel;
import com.egap.backend.repo.TuningModelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/dual_item_tags")
public class DualItemTagsController {
    private final TuningModelRepository repo;

    public DualItemTagsController(TuningModelRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(value = "status", required = false) String status) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TuningModel m : repo.findAll()) {
            String s = Optional.ofNullable(m.getStatus()).orElse("active");
            boolean include;
            if (status == null || status.isBlank()) include = !"archived".equalsIgnoreCase(s);
            else if ("all".equalsIgnoreCase(status)) include = true;
            else include = s.equalsIgnoreCase(status);
            if (include) {
                Map<String, Object> r = new HashMap<>();
                r.put("id", m.getId());
                r.put("name", m.getName());
                r.put("creator", m.getCreator());
                r.put("created_at", Optional.ofNullable(m.getCreatedAt()).orElse(Instant.now()));
                r.put("status", s);
                r.put("remark", m.getRemark());
                r.put("meta", m.getMeta());
                rows.add(r);
            }
        }
        return Map.of("rows", rows);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String name = Optional.ofNullable((String) body.get("name")).orElse("").trim();
        String creator = Optional.ofNullable((String) body.get("creator")).orElse("");
        if (name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(Map.of("error", "name required")));
        }
        Optional<TuningModel> exists = repo.findByNameIgnoreCase(name);
        if (exists.isPresent()) {
            Map<String, Object> r = new HashMap<>();
            r.put("error", "name exists");
            r.put("id", exists.get().getId());
            r.put("status", Optional.ofNullable(exists.get().getStatus()).orElse("archived"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(r);
        }
        TuningModel m = new TuningModel();
        m.setName(name);
        m.setCreator(creator);
        try {
            Object ts = body.get("createdAt");
            if (ts instanceof Number) {
                m.setCreatedAt(Instant.ofEpochMilli(((Number) ts).longValue()));
            } else if (ts instanceof String && !((String) ts).isBlank()) {
                m.setCreatedAt(Instant.ofEpochMilli(Long.parseLong((String) ts)));
            } else {
                m.setCreatedAt(Instant.now());
            }
        } catch (Exception e) {
            m.setCreatedAt(Instant.now());
        }
        m.setStatus(Optional.ofNullable((String) body.get("status")).orElse("archived"));
        try {
            Object metaObj = body.get("meta");
            if (metaObj instanceof Map) {
                m.setMeta((Map<String, Object>) metaObj);
            } else if (metaObj instanceof String) {
                Map<String, Object> parsed = new ObjectMapper().readValue((String) metaObj, Map.class);
                m.setMeta(parsed);
            }
        } catch (Exception ignored) {}
        String remark = Optional.ofNullable((String) body.get("remark")).orElse(null);
        if (remark == null && m.getMeta() != null) {
            Object note = m.getMeta().get("note");
            if (note instanceof String) remark = (String) note;
        }
        m.setRemark(remark);
        m = repo.save(m);
        Map<String, Object> r = new HashMap<>();
        r.put("id", m.getId());
        r.put("name", m.getName());
        r.put("creator", m.getCreator());
        r.put("created_at", m.getCreatedAt());
        r.put("status", m.getStatus());
        r.put("remark", m.getRemark());
        return ResponseEntity.ok(r);
    }

    @PutMapping("/{idOrName}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable("idOrName") String idOrName,
                                                      @RequestBody Map<String, Object> body) {
        Optional<TuningModel> target;
        try {
            Long id = Long.parseLong(idOrName);
            target = repo.findById(id);
        } catch (NumberFormatException e) {
            target = repo.findByNameIgnoreCase(idOrName);
        }
        if (target.isEmpty()) {
            Map<String, Object> r = new HashMap<>();
            r.put("error", "not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(r);
        }
        TuningModel m = target.get();
        {
            String newName = Optional.ofNullable((String) body.get("name")).orElse("").trim();
            if (!newName.isBlank()) {
                if (repo.findByNameIgnoreCase(newName).isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new HashMap<>(Map.of("error", "name exists")));
                }
                m.setName(newName);
            }
            if (body.containsKey("status")) {
                String s = Optional.ofNullable((String) body.get("status")).orElse("").trim();
                if (!s.isBlank()) m.setStatus(s);
            }
            if (body.containsKey("meta")) {
                try {
                    Object metaObj = body.get("meta");
                    if (metaObj instanceof Map) {
                        m.setMeta((Map<String, Object>) metaObj);
                    } else if (metaObj instanceof String) {
                        Map<String, Object> parsed = new ObjectMapper().readValue((String) metaObj, Map.class);
                        m.setMeta(parsed);
                    }
                } catch (Exception ignored) {}
            }
            if (body.containsKey("remark")) {
                String rm = Optional.ofNullable((String) body.get("remark")).orElse(null);
                m.setRemark(rm);
            }
            repo.save(m);
            Map<String, Object> r = new HashMap<>();
            r.put("ok", Boolean.TRUE);
            r.put("id", m.getId());
            r.put("name", m.getName());
            r.put("status", m.getStatus());
            r.put("remark", m.getRemark());
            return ResponseEntity.ok(r);
        }
    }

    @PutMapping("/{idOrName}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable("idOrName") String idOrName,
                                                            @RequestBody Map<String, Object> body) {
        Optional<TuningModel> target;
        try {
            Long id = Long.parseLong(idOrName);
            target = repo.findById(id);
        } catch (NumberFormatException e) {
            target = repo.findByNameIgnoreCase(idOrName);
        }
        String status = Optional.ofNullable((String) body.get("status")).orElse("").trim();
        if (status.isBlank()) {
            Map<String, Object> r = new HashMap<>();
            r.put("error", "status required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
        }
        if (target.isEmpty()) {
            Map<String, Object> r = new HashMap<>();
            r.put("error", "not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(r);
        }
        TuningModel m = target.get();
        m.setStatus(status);
        repo.save(m);
        Map<String, Object> r = new HashMap<>();
        r.put("ok", Boolean.TRUE);
        return ResponseEntity.ok(r);
    }
}
