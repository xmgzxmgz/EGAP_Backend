package com.egap.backend.controller;

import com.egap.backend.model.EnterpriseInfo;
import com.egap.backend.model.EnterpriseTag;
import com.egap.backend.model.Tag;
import com.egap.backend.repo.EnterpriseInfoRepository;
import com.egap.backend.repo.EnterpriseTagRepository;
import com.egap.backend.repo.TagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        List<EnterpriseInfo> list = (category == null || category.isBlank())
                ? repository.findAll()
                : repository.findByCategoryIgnoreCase(category);

        return Map.of("rows", list);
    }

    @PostMapping("/enterprise/tags")
    public Map<String, Object> addEnterpriseTag(@RequestParam("enterpriseId") Long enterpriseId,
                                                @RequestParam("tagId") Long tagId,
                                                @RequestParam(value = "createdBy", required = false) String createdBy) {
        EnterpriseInfo enterprise = repository.findById(enterpriseId).orElseThrow(() -> new NoSuchElementException("enterprise not found"));
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new NoSuchElementException("tag not found"));
        try {
            EnterpriseTag et = new EnterpriseTag(enterprise, tag, createdBy == null ? "system" : createdBy);
            enterpriseTagRepository.save(et);
        } catch (Exception ignored) {
        }
        return Map.of("ok", true);
    }
}
