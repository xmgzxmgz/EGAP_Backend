package com.egap.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "val")
    private Integer val;

    public Tag() {}

    public Tag(String name, String description, Integer val) {
        this.name = name;
        this.description = description;
        this.val = val;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("desc")
    public String getDescription() {
        return description;
    }

    @JsonProperty("value")
    public Integer getVal() {
        return val;
    }
}