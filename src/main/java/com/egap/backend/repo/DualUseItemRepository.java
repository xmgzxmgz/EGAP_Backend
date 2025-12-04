package com.egap.backend.repo;

import com.egap.backend.model.DualUseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DualUseItemRepository extends JpaRepository<DualUseItem, Integer>, JpaSpecificationExecutor<DualUseItem> {
}
