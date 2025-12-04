package com.egap.backend.controller;

import com.egap.backend.model.Tag;
import com.egap.backend.repo.TagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.cache.annotation.Cacheable;

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
                rows.add(Map.of(
                        "name", t.getName(),
                        "desc", t.getDescription()
                ));
            }
        } catch (Exception ignored) {
            rows = List.of(
                    Map.of("name", "合规", "desc", "合规相关标签"),
                    Map.of("name", "风险", "desc", "风险评估与预警"),
                    Map.of("name", "两用物项", "desc", "军民两用敏感物项"),
                    Map.of("name", "供应链", "desc", "供应链稳定性与安全"),
                    Map.of("name", "贸易管制", "desc", "国际贸易管制相关")
            );
        }
        return Map.of("rows", rows);
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
            rows = List.of(
                    Map.of("name", "合规", "value", 120),
                    Map.of("name", "风险", "value", 180),
                    Map.of("name", "两用物项", "value", 95),
                    Map.of("name", "供应链", "value", 140),
                    Map.of("name", "贸易管制", "value", 70)
            );
        }
        return Map.of("rows", rows);
    }
}
