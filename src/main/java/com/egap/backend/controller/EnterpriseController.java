package com.egap.backend.controller;

import com.egap.backend.model.EnterpriseInfo;
import com.egap.backend.repo.EnterpriseInfoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class EnterpriseController {
    private final EnterpriseInfoRepository repository;

    public EnterpriseController(EnterpriseInfoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/enterprise/basic-info")
    public Map<String, Object> getEnterpriseBasicInfo(@RequestParam(value = "category", required = false) String category) {
        List<EnterpriseInfo> list = (category == null || category.isBlank())
                ? repository.findAll()
                : repository.findByCategoryIgnoreCase(category);

        return Map.of("rows", list);
    }
}