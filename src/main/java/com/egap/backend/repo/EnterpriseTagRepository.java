package com.egap.backend.repo;

import com.egap.backend.model.EnterpriseTag;
import com.egap.backend.model.EnterpriseTagId;
import com.egap.backend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterpriseTagRepository extends JpaRepository<EnterpriseTag, EnterpriseTagId> {
    long countByTag(Tag tag);
}

