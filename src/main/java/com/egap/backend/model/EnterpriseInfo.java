package com.egap.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "enterprise_info")
public class EnterpriseInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private Double risk;

    public EnterpriseInfo() {}

    public EnterpriseInfo(String name, String category, String region, Double risk) {
        this.name = name;
        this.category = category;
        this.region = region;
        this.risk = risk;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getRegion() {
        return region;
    }

    public Double getRisk() {
        return risk;
    }
}