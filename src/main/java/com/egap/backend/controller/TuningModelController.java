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
@RequestMapping("/api/tuning/models")
public class TuningModelController {
    private final TuningModelRepository repo;

    public TuningModelController(TuningModelRepository repo) {
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
                r.put("meta", m.getMeta());
                rows.add(r);
            }
        }
        return Map.of("rows", rows);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable("id") Long id) {
        return repo.findById(id)
                .map(m -> {
                    Map<String, Object> r = new HashMap<>();
                    r.put("id", m.getId());
                    r.put("name", m.getName());
                    r.put("creator", m.getCreator());
                    r.put("created_at", m.getCreatedAt());
                    r.put("status", m.getStatus());
                    r.put("meta", m.getMeta());
                    return ResponseEntity.ok(r);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(Map.of("error", "not found"))));
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
        m.setCreatedAt(Instant.now());
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
        m = repo.save(m);
        Map<String, Object> r = new HashMap<>();
        r.put("id", m.getId());
        r.put("name", m.getName());
        r.put("creator", m.getCreator());
        r.put("created_at", m.getCreatedAt());
        r.put("status", m.getStatus());
        return ResponseEntity.ok(r);
    }

    @PutMapping("/{idOrName}/rename")
    public ResponseEntity<Map<String, Object>> rename(@PathVariable("idOrName") String idOrName,
                                                      @RequestBody Map<String, Object> body) {
        String newName = Optional.ofNullable((String) body.get("name")).orElse("").trim();
        if (newName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(Map.of("error", "name required")));
        }
        if (repo.findByNameIgnoreCase(newName).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new HashMap<>(Map.of("error", "name exists")));
        }
        Optional<TuningModel> target;
        try {
            Long id = Long.parseLong(idOrName);
            target = repo.findById(id);
        } catch (NumberFormatException e) {
            target = repo.findByNameIgnoreCase(idOrName);
        }
        return target.map(m -> {
            m.setName(newName);
            repo.save(m);
            Map<String, Object> r = new HashMap<>();
            r.put("id", m.getId());
            r.put("name", m.getName());
            return ResponseEntity.ok(r);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(Map.of("error", "not found"))));
    }

    @DeleteMapping("/{idOrName}")
    public ResponseEntity<Map<String, Object>> remove(@PathVariable("idOrName") String idOrName) {
        Optional<TuningModel> target;
        try {
            Long id = Long.parseLong(idOrName);
            target = repo.findById(id);
        } catch (NumberFormatException e) {
            target = repo.findByNameIgnoreCase(idOrName);
        }
        return target.map(m -> {
            m.setStatus("archived");
            repo.save(m);
            Map<String, Object> r = new HashMap<>();
            r.put("ok", Boolean.TRUE);
            return ResponseEntity.ok(r);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(Map.of("error", "not found"))));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable("id") Long id,
                                                            @RequestBody Map<String, Object> body) {
        String status = Optional.ofNullable((String) body.get("status")).orElse("").trim();
        if (status.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(Map.of("error", "status required")));
        }
        return repo.findById(id).map(m -> {
            m.setStatus(status);
            repo.save(m);
            Map<String, Object> r = new HashMap<>();
            r.put("ok", Boolean.TRUE);
            return ResponseEntity.ok(r);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(Map.of("error", "not found"))));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMeta(@PathVariable("id") Long id,
                                                          @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(m -> {
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
            if (body.containsKey("status")) {
                String s = Optional.ofNullable((String) body.get("status")).orElse("").trim();
                if (!s.isBlank()) m.setStatus(s);
            }
            repo.save(m);
            Map<String, Object> r = new HashMap<>();
            r.put("ok", Boolean.TRUE);
            r.put("id", m.getId());
            r.put("status", m.getStatus());
            return ResponseEntity.ok(r);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(Map.of("error", "not found"))));
    }
}
