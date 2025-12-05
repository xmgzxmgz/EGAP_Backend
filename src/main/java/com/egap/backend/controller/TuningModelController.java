package com.egap.backend.controller;

import com.egap.backend.model.TuningModel;
import com.egap.backend.repo.TuningModelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Map<String, Object> list() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TuningModel m : repo.findAll()) {
            if (!"archived".equalsIgnoreCase(Optional.ofNullable(m.getStatus()).orElse("active"))) {
                rows.add(Map.of(
                        "id", m.getId(),
                        "name", m.getName(),
                        "creator", m.getCreator(),
                        "created_at", Optional.ofNullable(m.getCreatedAt()).orElse(Instant.now()),
                        "status", Optional.ofNullable(m.getStatus()).orElse("active")
                ));
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
        if (repo.findByNameIgnoreCase(name).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new HashMap<>(Map.of("error", "name exists")));
        }
        TuningModel m = new TuningModel();
        m.setName(name);
        m.setCreator(creator);
        m.setCreatedAt(Instant.now());
        m.setStatus("active");
        m.setMeta((String) body.get("meta"));
        m = repo.save(m);
        Map<String, Object> r = new HashMap<>();
        r.put("id", m.getId());
        r.put("name", m.getName());
        r.put("creator", m.getCreator());
        r.put("created_at", m.getCreatedAt());
        return ResponseEntity.ok(r);
    }

    @PutMapping("/{idOrName}")
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
}
