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

    @Column(name = "color")
    private String color;

    @Column(name = "source")
    private String source;

    @Column(name = "created_at")
    private java.time.Instant createdAt = java.time.Instant.now();

    @Column(name = "updated_at")
    private java.time.Instant updatedAt = java.time.Instant.now();

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

    public String getColor() { return color; }
    public String getSource() { return source; }
    public java.time.Instant getCreatedAt() { return createdAt; }
    public java.time.Instant getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setVal(Integer val) { this.val = val; }
    public void setColor(String color) { this.color = color; }
    public void setSource(String source) { this.source = source; }
    public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(java.time.Instant updatedAt) { this.updatedAt = updatedAt; }
}
