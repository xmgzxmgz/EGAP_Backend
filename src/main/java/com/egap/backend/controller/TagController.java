package com.egap.backend.controller;

import com.egap.backend.model.Tag;
import com.egap.backend.repo.TagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TagController {
    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping("/tags")
    public Map<String, Object> getTags(@RequestParam(value = "query", required = false) String query) {
        List<Tag> list = (query == null || query.isBlank())
                ? tagRepository.findAll()
                : tagRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Tag t : list) {
            rows.add(Map.of(
                    "name", t.getName(),
                    "desc", t.getDescription()
            ));
        }
        return Map.of("rows", rows);
    }

    @GetMapping("/tags/distribution")
    public Map<String, Object> getTagDistribution() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Tag t : tagRepository.findAll()) {
            rows.add(Map.of(
                    "name", t.getName(),
                    "value", Optional.ofNullable(t.getVal()).orElse(0)
            ));
        }
        return Map.of("rows", rows);
    }
}