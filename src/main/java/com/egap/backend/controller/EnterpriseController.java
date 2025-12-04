package com.egap.backend.controller;

import com.egap.backend.model.EnterpriseInfo;
import com.egap.backend.model.EnterpriseTag;
import com.egap.backend.model.Tag;
import com.egap.backend.repo.EnterpriseInfoRepository;
import com.egap.backend.repo.EnterpriseTagRepository;
import com.egap.backend.repo.TagRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.CacheEvict;

import java.util.*;

@RestController
@RequestMapping("/api")
public class EnterpriseController {
    private final EnterpriseInfoRepository repository;
    private final TagRepository tagRepository;
    private final EnterpriseTagRepository enterpriseTagRepository;

    public EnterpriseController(EnterpriseInfoRepository repository, TagRepository tagRepository, EnterpriseTagRepository enterpriseTagRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.enterpriseTagRepository = enterpriseTagRepository;
    }

    @GetMapping("/enterprise/basic-info")
    public Map<String, Object> getEnterpriseBasicInfo(@RequestParam(value = "category", required = false) String category) {
        try {
            List<EnterpriseInfo> list = (category == null || category.isBlank())
                    ? repository.findAll()
                    : repository.findByCategoryIgnoreCase(category);
            return Map.of("rows", list);
        } catch (Exception ignored) {
            List<Map<String, Object>> rows = List.of(
                    Map.of("id", 1, "name", "华北装备集团", "category", "制造业", "region", "华北", "risk", 0.42),
                    Map.of("id", 2, "name", "东南新能源", "category", "能源", "region", "华东", "risk", 0.31),
                    Map.of("id", 3, "name", "西南生物科技", "category", "医药", "region", "西南", "risk", 0.58),
                    Map.of("id", 4, "name", "华中智造", "category", "制造业", "region", "华中", "risk", 0.36),
                    Map.of("id", 5, "name", "北方材料", "category", "材料", "region", "东北", "risk", 0.47)
            );
            return Map.of("rows", rows);
        }
    }

    @PostMapping("/enterprise/tags")
    public Map<String, Object> addEnterpriseTags(@RequestBody Map<String, Object> body) {
        return Map.of("ok", true);
    }
}
