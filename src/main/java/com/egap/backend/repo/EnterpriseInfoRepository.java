package com.egap.backend.repo;

import com.egap.backend.model.EnterpriseInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnterpriseInfoRepository extends JpaRepository<EnterpriseInfo, Long> {
    List<EnterpriseInfo> findByCategoryIgnoreCase(String category);
    List<EnterpriseInfo> findByNameContainingIgnoreCase(String name);
}