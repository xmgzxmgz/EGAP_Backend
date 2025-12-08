package com.egap.backend.controller;

import com.egap.backend.model.EnterpriseInfo;
import com.egap.backend.model.Tag;
import com.egap.backend.repo.EnterpriseInfoRepository;
import com.egap.backend.repo.EnterpriseTagRepository;
import com.egap.backend.repo.TagRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@RestController
@RequestMapping("/api")
public class EnterpriseController {
    private final EnterpriseInfoRepository repository;
    private final TagRepository tagRepository;
    private final EnterpriseTagRepository enterpriseTagRepository;
    private final JdbcTemplate jdbcTemplate;

    public EnterpriseController(EnterpriseInfoRepository repository, TagRepository tagRepository, EnterpriseTagRepository enterpriseTagRepository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.enterpriseTagRepository = enterpriseTagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/enterprise/basic-info")
    public Map<String, Object> getEnterpriseBasicInfo(@RequestParam(value = "category", required = false) String category) {
        try {
            List<EnterpriseInfo> list = (category == null || category.isBlank())
                    ? repository.findAll()
                    : repository.findByCategoryIgnoreCase(category);
            return Map.of("rows", list);
        } catch (Exception ignored) {
            return Map.of("rows", List.of());
        }
    }

    @PostMapping("/enterprise/tags")
    public Map<String, Object> addEnterpriseTags(@RequestBody Map<String, Object> body) {
        String name = Optional.ofNullable((String) body.get("name")).orElse("").trim();
        List<?> tagsList = Optional.ofNullable((List<?>) body.get("tags")).orElse(Collections.emptyList());
        String source = Optional.ofNullable((String) body.get("source")).orElse("manual");
        Map<String, Object> result = new HashMap<>();
        if (name.isBlank() || tagsList.isEmpty()) {
            result.put("ok", false);
            result.put("error", "name and tags required");
            return result;
        }
        try {
            com.egap.backend.model.EnterpriseInfo ent = repository.findFirstByNameIgnoreCase(name)
                    .orElseGet(() -> repository.save(new EnterpriseInfo(name, "未知", "未知", 0.0)));
            int affected = 0;
            for (Object tObj : tagsList) {
                String tName = String.valueOf(tObj);
                com.egap.backend.model.Tag tag = tagRepository.findByNameIgnoreCase(tName)
                        .orElseGet(() -> {
                            Tag nt = new Tag(tName, null, null);
                            nt.setSource(source);
                            nt.setCreatedAt(java.time.Instant.now());
                            nt.setUpdatedAt(java.time.Instant.now());
                            return tagRepository.save(nt);
                        });
                affected += jdbcTemplate.update(
                        "INSERT INTO enterprise_tags(enterprise_id, tag_id, created_by, created_at) VALUES(?, ?, ?, now()) " +
                                "ON CONFLICT (enterprise_id, tag_id) DO UPDATE SET created_by = EXCLUDED.created_by, created_at = now()",
                        ent.getId(), tag.getId(), source
                );
            }
            result.put("ok", true);
            result.put("count", affected);
            return result;
        } catch (Exception e) {
            result.put("ok", false);
            result.put("error", "upsert failed");
            return result;
        }
    }
}
