package com.egap.backend.repo;

import com.egap.backend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    java.util.Optional<Tag> findByNameIgnoreCase(String name);
}
