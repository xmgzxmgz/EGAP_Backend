package com.egap.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "tuning_models")
public class TuningModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String creator;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column
    private String status = "archived";

    @Column
    private String remark;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> meta;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCreator() { return creator; }
    public Instant getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public String getRemark() { return remark; }
    public Map<String, Object> getMeta() { return meta; }

    public void setName(String name) { this.name = name; }
    public void setCreator(String creator) { this.creator = creator; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setRemark(String remark) { this.remark = remark; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}
