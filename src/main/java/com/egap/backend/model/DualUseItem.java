package com.egap.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dual_use_items")
public class DualUseItem {
    @Id
    @Column(name = "trade_co")
    private String tradeCo;

    @Column(name = "\"Regulatory Authority\"")
    private String regulatoryAuthority;

    @Column(name = "\"Registration Location\"")
    private String registrationLocation;

    @Column(name = "\"Enterprise Type (Nature)\"")
    private String enterpriseTypeNature;

    @Column(name = "\"Enterprise Type (Industry)\"")
    private String enterpriseTypeIndustry;

    @Column(name = "\"Industry Category\"")
    private String industryCategory;

    @Column(name = "\"Customs Broker\"")
    private String customsBroker;

    @Column(name = "\"Consignee Enterprise\"")
    private String consigneeEnterprise;

    @Column(name = "\"Specialized, Refined, Unique, New\"")
    private String srun;

    @Column(name = "\"Registered Capital (10k CNY)\"")
    private BigDecimal registeredCapital;

    @Column(name = "\"Paid-in Capital (10k CNY)\"")
    private BigDecimal paidInCapital;

    @Column(name = "\"Legal Person Risk\"")
    private String legalPersonRisk;

    @Column(name = "\"Current Year Import/Export Amount (10k CNY)\"")
    private BigDecimal currentYearImportExportAmt;

    @Column(name = "\"Past Three Years Import/Export Amount (10k CNY)\"")
    private BigDecimal pastThreeYearsImportExportAmt;

    @Column(name = "\"Current Year Import/Export Growth Rate\"")
    private BigDecimal currentYearImportExportGrowthRate;

    @Column(name = "\"Current Year Tax Amount (10k CNY)\"")
    private BigDecimal currentYearTaxAmt;

    @Column(name = "\"Past Three Years Tax Amount (10k CNY)\"")
    private BigDecimal pastThreeYearsTaxAmt;

    @Column(name = "\"Supervision_Current Year Import/Export Amount (10k CNY)\"")
    private BigDecimal supervisionCurrentYearImportExportAmt;

    @Column(name = "\"Supervision_Past Three Years Import/Export Amount (10k CNY)\"")
    private BigDecimal supervisionPastThreeYearsImportExportAmt;

    @Column(name = "\"Supervision_Current Year Import/Export Growth Rate\"")
    private BigDecimal supervisionCurrentYearImportExportGrowthRate;

    @Column(name = "\"Settlement Exchange Rate\"")
    private BigDecimal settlementExchangeRate;

    @Column(name = "\"Current Year Customs Enforcement Count\"")
    private Integer currentYearCustomsEnforcementCount;

    @Column(name = "\"Previous Year Customs Enforcement Count\"")
    private Integer previousYearCustomsEnforcementCount;

    @Column(name = "\"Current Year Anomaly Count\"")
    private Integer currentYearAnomalyCount;

    @Column(name = "\"Past Three Years Anomaly Count\"")
    private Integer pastThreeYearsAnomalyCount;

    public String getTradeCo() { return tradeCo; }
    public String getRegulatoryAuthority() { return regulatoryAuthority; }
    public String getRegistrationLocation() { return registrationLocation; }
    public String getEnterpriseTypeNature() { return enterpriseTypeNature; }
    public String getEnterpriseTypeIndustry() { return enterpriseTypeIndustry; }
    public String getIndustryCategory() { return industryCategory; }
    public String getCustomsBroker() { return customsBroker; }
    public String getConsigneeEnterprise() { return consigneeEnterprise; }
    public String getSrun() { return srun; }
    public BigDecimal getRegisteredCapital() { return registeredCapital; }
    public BigDecimal getPaidInCapital() { return paidInCapital; }
    public String getLegalPersonRisk() { return legalPersonRisk; }
    public BigDecimal getCurrentYearImportExportAmt() { return currentYearImportExportAmt; }
    public BigDecimal getPastThreeYearsImportExportAmt() { return pastThreeYearsImportExportAmt; }
    public BigDecimal getCurrentYearImportExportGrowthRate() { return currentYearImportExportGrowthRate; }
    public BigDecimal getCurrentYearTaxAmt() { return currentYearTaxAmt; }
    public BigDecimal getPastThreeYearsTaxAmt() { return pastThreeYearsTaxAmt; }
    public BigDecimal getSupervisionCurrentYearImportExportAmt() { return supervisionCurrentYearImportExportAmt; }
    public BigDecimal getSupervisionPastThreeYearsImportExportAmt() { return supervisionPastThreeYearsImportExportAmt; }
    public BigDecimal getSupervisionCurrentYearImportExportGrowthRate() { return supervisionCurrentYearImportExportGrowthRate; }
    public BigDecimal getSettlementExchangeRate() { return settlementExchangeRate; }
    public Integer getCurrentYearCustomsEnforcementCount() { return currentYearCustomsEnforcementCount; }
    public Integer getPreviousYearCustomsEnforcementCount() { return previousYearCustomsEnforcementCount; }
    public Integer getCurrentYearAnomalyCount() { return currentYearAnomalyCount; }
    public Integer getPastThreeYearsAnomalyCount() { return pastThreeYearsAnomalyCount; }
}
