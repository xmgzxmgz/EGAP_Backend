package com.egap.backend.controller;

import com.egap.backend.model.Tag;
import com.egap.backend.repo.TagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TagController {
    private final TagRepository tagRepository;
    private final com.egap.backend.repo.EnterpriseTagRepository enterpriseTagRepository;

    public TagController(TagRepository tagRepository, com.egap.backend.repo.EnterpriseTagRepository enterpriseTagRepository) {
        this.tagRepository = tagRepository;
        this.enterpriseTagRepository = enterpriseTagRepository;
    }

    @GetMapping("/tags")
    public Map<String, Object> getTags(@RequestParam(value = "query", required = false) String query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            List<Tag> list = (query == null || query.isBlank())
                    ? tagRepository.findAll()
                    : tagRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
            for (Tag t : list) {
                Map<String, Object> r = new HashMap<>();
                r.put("id", t.getId());
                r.put("name", t.getName());
                r.put("desc", t.getDescription());
                r.put("source", Optional.ofNullable(t.getSource()).orElse("manual"));
                rows.add(r);
            }
        } catch (Exception ignored) {
            rows = List.of();
        }
        return Map.of("rows", rows);
    }

    @PostMapping("/tags")
    public Map<String, Object> createOrUpdateTag(@RequestBody Map<String, Object> body) {
        String name = Optional.ofNullable((String) body.get("name")).orElse("").trim();
        String desc = Optional.ofNullable((String) body.get("description")).orElse(null);
        String color = Optional.ofNullable((String) body.get("color")).orElse(null);
        String source = Optional.ofNullable((String) body.get("source")).orElse("manual");
        Map<String, Object> r = new HashMap<>();
        if (name.isBlank()) {
            r.put("ok", false);
            r.put("error", "name required");
            return r;
        }
        try {
            Tag t = tagRepository.findByNameIgnoreCase(name).orElseGet(Tag::new);
            t.setName(name);
            t.setDescription(desc);
            t.setColor(color);
            t.setSource(source);
            t.setUpdatedAt(java.time.Instant.now());
            t = tagRepository.save(t);
            r.put("ok", true);
            r.put("id", t.getId());
            return r;
        } catch (Exception e) {
            r.put("ok", false);
            r.put("error", "save failed");
            return r;
        }
    }

    @GetMapping("/tags/distribution")
    public Map<String, Object> getTagDistribution() {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            for (Tag t : tagRepository.findAll()) {
                int value = Optional.ofNullable(t.getVal()).orElse(0);
                rows.add(Map.of(
                        "name", t.getName(),
                        "value", value
                ));
            }
        } catch (Exception ignored) {
            rows = List.of();
        }
        return Map.of("rows", rows);
    }
}
