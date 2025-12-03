package com.egap.backend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EnterpriseTagId implements Serializable {
    private Long enterpriseId;
    private Long tagId;

    public EnterpriseTagId() {}

    public EnterpriseTagId(Long enterpriseId, Long tagId) {
        this.enterpriseId = enterpriseId;
        this.tagId = tagId;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public Long getTagId() {
        return tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnterpriseTagId that = (EnterpriseTagId) o;
        return Objects.equals(enterpriseId, that.enterpriseId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enterpriseId, tagId);
    }
}

