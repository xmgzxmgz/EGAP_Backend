package com.egap.backend.repo;

import com.egap.backend.model.EtpsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EtpsDataRepository extends JpaRepository<EtpsData, Long>, JpaSpecificationExecutor<EtpsData> {
}
