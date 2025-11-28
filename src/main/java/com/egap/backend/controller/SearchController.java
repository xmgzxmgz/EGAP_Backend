package com.egap.backend.controller;

import com.egap.backend.model.EnterpriseInfo;
import com.egap.backend.model.Tag;
import com.egap.backend.repo.EnterpriseInfoRepository;
import com.egap.backend.repo.TagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final TagRepository tagRepository;
    private final EnterpriseInfoRepository enterpriseRepository;

    public SearchController(TagRepository tagRepository, EnterpriseInfoRepository enterpriseRepository) {
        this.tagRepository = tagRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam("q") String q) {
        List<Map<String, String>> rows = new ArrayList<>();

        List<Tag> tagMatches = tagRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q);
        for (Tag t : tagMatches) {
            rows.add(Map.of(
                    "path", "/tags/" + slug(t.getName()),
                    "title", t.getName()
            ));
        }

        List<EnterpriseInfo> entMatches = enterpriseRepository.findByNameContainingIgnoreCase(q);
        for (EnterpriseInfo e : entMatches) {
            rows.add(Map.of(
                    "path", "/enterprise/" + slug(e.getName()),
                    "title", e.getName()
            ));
        }

        return Map.of("rows", rows);
    }

    private String slug(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}