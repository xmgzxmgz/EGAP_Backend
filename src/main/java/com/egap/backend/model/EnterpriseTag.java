package com.egap.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "enterprise_tags")
public class EnterpriseTag {
    @EmbeddedId
    private EnterpriseTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("enterpriseId")
    @JoinColumn(name = "enterprise_id")
    private EnterpriseInfo enterprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public EnterpriseTag() {}

    public EnterpriseTag(EnterpriseInfo enterprise, Tag tag, String createdBy) {
        this.id = new EnterpriseTagId(enterprise.getId(), tag.getId());
        this.enterprise = enterprise;
        this.tag = tag;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public EnterpriseTagId getId() {
        return id;
    }

    public Tag getTag() {
        return tag;
    }

    public EnterpriseInfo getEnterprise() {
        return enterprise;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

