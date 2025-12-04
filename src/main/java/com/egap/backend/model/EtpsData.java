package com.egap.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "etps_data")
public class EtpsData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etps_name")
    private String etpsName;

    @Column(name = "industry_phy_name")
    private String industryPhyName;

    @Column(name = "industry_code_name")
    private String industryCodeName;

    @Column(name = "area_id")
    private String areaId;

    @Column(name = "exist_status")
    private String existStatus;

    @Column(name = "common_busi")
    private String commonBusi;

    @Column(name = "import_ratio")
    private BigDecimal importRatio;

    @Column(name = "main_ciq_codes")
    private String mainCiqCodes;

    @Column(name = "main_parent_ciq")
    private String mainParentCiq;

    @Column(name = "top_trade_countries")
    private String topTradeCountries;

    @Column(name = "transport_mode")
    private String transportMode;

    @Column(name = "total_decl_amt")
    private BigDecimal totalDeclAmt;

    @Column(name = "total_entry_cnt")
    private Integer totalEntryCnt;

    @Column(name = "avg_ticket_val")
    private BigDecimal avgTicketVal;

    @Column(name = "aeo_rating")
    private String aeoRating;

    @Column(name = "delay_rate")
    private BigDecimal delayRate;

    public Long getId() { return id; }
    public String getEtpsName() { return etpsName; }
    public String getIndustryPhyName() { return industryPhyName; }
    public String getIndustryCodeName() { return industryCodeName; }
    public String getAreaId() { return areaId; }
    public String getExistStatus() { return existStatus; }
    public String getCommonBusi() { return commonBusi; }
    public BigDecimal getImportRatio() { return importRatio; }
    public String getMainCiqCodes() { return mainCiqCodes; }
    public String getMainParentCiq() { return mainParentCiq; }
    public String getTopTradeCountries() { return topTradeCountries; }
    public String getTransportMode() { return transportMode; }
    public BigDecimal getTotalDeclAmt() { return totalDeclAmt; }
    public Integer getTotalEntryCnt() { return totalEntryCnt; }
    public BigDecimal getAvgTicketVal() { return avgTicketVal; }
    public String getAeoRating() { return aeoRating; }
    public BigDecimal getDelayRate() { return delayRate; }
}

