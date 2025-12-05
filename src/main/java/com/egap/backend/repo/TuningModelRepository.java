package com.egap.backend.repo;

import com.egap.backend.model.TuningModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TuningModelRepository extends JpaRepository<TuningModel, Long> {
    Optional<TuningModel> findByNameIgnoreCase(String name);
}

