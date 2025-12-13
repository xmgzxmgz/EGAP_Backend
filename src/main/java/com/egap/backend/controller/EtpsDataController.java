package com.egap.backend.controller;

import com.egap.backend.model.DualUseItem;
import com.egap.backend.repo.DualUseItemRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EtpsDataController {
    private final DualUseItemRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public EtpsDataController(DualUseItemRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/dual_use_items")
    public Map<String, Object> getEtpsData(@RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "20") int size,
                                           @RequestParam(value = "q", required = false) String q,
                                           @RequestParam(value = "areaId", required = false) String areaId) {
        try {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        Map<String, Object> result = new HashMap<>();
        try {
            List<String> present = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name='dual_use_items'",
                    String.class
            );
            String[] specs = new String[]{
                    "trade_co",
                    "uniscid",
                    "etps_name",
                    "industry_phy_name",
                    "industry_code_name",
                    "area_id",
                    "reg_prov_name",
                    "reg_city_name",
                    "etps_estb_year",
                    "corp_type",
                    "staff_scale",
                    "aeo_level",
                    "busi_type",
                    "break_law_datetime1",
                    "break_law_datetime2",
                    "inspect_datetime",
                    "is_direct_metal_firm",
                    "is_shadow_firm",
                    "is_risk_word_export_firm",
                    "has_dual_use_license",
                    "is_network_firm",
                    "is_manual_chk_metal",
                    "is_scope_metal_firm",
                    "sm_export_entry_cnt_3y",
                    "sm_export_value_3y",
                    "sm_export_dest_country_cnt_3y",
                    "shadow_export_entry_cnt_3y",
                    "shadow_export_value_3y",
                    "all_export_entry_cnt_3y",
                    "all_export_value_3y",
                    "sm_export_value_ratio_all_3y",
                    "sm_export_entry_ratio_all_3y",
                    "shadow_export_value_ratio_all_3y",
                    "sm_unit_price_avg_3y",
                    "sm_value_density_avg_3y",
                    "shadow_unit_price_avg_3y",
                    "sm_vs_shadow_unit_price_ratio",
                    "sm_air_ratio_3y",
                    "sm_sensitive_dest_ratio_3y",
                    "sm_trade_months_active_3y",
                    "sm_trade_duration_months_3y",
                    "sm_avg_monthly_entry_cnt_3y",
                    "sm_monthly_entry_std_3y",
                    "sm_entry_cnt_12m",
                    "sm_entry_cnt_6m",
                    "sm_value_12m",
                    "sm_value_6m",
                    "sm_value_growth_6m_vs_prev_6m",
                    "shadow_entry_cnt_12m",
                    "shadow_value_12m",
                    "owner_cnt_3y",
                    "agent_cnt_3y",
                    "oversea_buyer_cnt_3y",
                    "sm_oversea_buyer_cnt_3y",
                    "sm_oversea_buyer_ratio_3y",
                    "audit_case_cnt_3y",
                    "audit_smuggling_flag",
                    "seized_case_cnt_3y",
                    "seized_entry_ratio_3y",
                    "sm_seized_case_cnt_3y",
                    "post_policy_seized_cnt_1p5y",
                    "dual_use_license_flag",
                    "dual_use_license_cnt_3y",
                    "dual_use_license_sm_ratio_3y",
                    "dual_use_license_sensitive_dest_ratio",
                    "dual_use_license_post_policy_cnt"
            };

            List<String> selectParts = new java.util.ArrayList<>();
            selectParts.add("trade_co");
            for (String col : specs) {
                if (present.contains(col)) {
                    if (!"trade_co".equals(col)) {
                        selectParts.add(col);
                    }
                }
            }

            String baseWhere = "";
            List<Object> params = new java.util.ArrayList<>();
            List<String> likeColumns = new java.util.ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                if (present.contains("etps_name")) likeColumns.add("lower(etps_name) LIKE ?");
                if (present.contains("trade_co")) likeColumns.add("lower(trade_co) LIKE ?");
                if (present.contains("uniscid")) likeColumns.add("lower(uniscid) LIKE ?");
                if (present.contains("reg_prov_name")) likeColumns.add("lower(reg_prov_name) LIKE ?");
                if (present.contains("reg_city_name")) likeColumns.add("lower(reg_city_name) LIKE ?");
                for (int i = 0; i < likeColumns.size(); i++) params.add(like);
                if (!likeColumns.isEmpty()) baseWhere = " WHERE " + String.join(" OR ", likeColumns);
            }
            if (areaId != null && !areaId.isBlank() && present.contains("area_id")) {
                String cond = "area_id::text = ?";
                if (baseWhere.isEmpty()) baseWhere = " WHERE " + cond;
                else baseWhere = baseWhere + " AND " + cond;
                params.add(areaId);
            }

            String selectSql = "SELECT " + String.join(", ", selectParts) + " FROM dual_use_items" + baseWhere + " ORDER BY trade_co LIMIT ? OFFSET ?";
            String countSql = "SELECT count(*) FROM dual_use_items" + baseWhere;

            long total = params.isEmpty()
                    ? jdbcTemplate.queryForObject(countSql, Long.class)
                    : jdbcTemplate.queryForObject(countSql, params.toArray(), Long.class);

            params.add(size);
            params.add(page * size);

            List<Map<String, Object>> rows = params.size() > 2
                    ? jdbcTemplate.queryForList(selectSql, params.toArray())
                    : jdbcTemplate.queryForList(selectSql, size, page * size);
            result.put("rows", rows);
            result.put("total", total);
        } catch (Exception e) {
            result.put("rows", List.of());
            result.put("total", 0);
            result.put("error", String.valueOf(e.getMessage()));
        }
        return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("rows", List.of());
            result.put("total", 0);
            result.put("error", String.valueOf(e.getMessage()));
            return result;
        }
    }

    @GetMapping("/dual_use_items/all")
    public Map<String, Object> getAllEtpsData(@RequestParam(value = "q", required = false) String q) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> present = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name='dual_use_items'",
                    String.class
            );
            String[] specs = new String[]{
                    "trade_co",
                    "uniscid",
                    "etps_name",
                    "industry_phy_name",
                    "industry_code_name",
                    "area_id",
                    "reg_prov_name",
                    "reg_city_name",
                    "etps_estb_year",
                    "corp_type",
                    "staff_scale",
                    "aeo_level",
                    "busi_type",
                    "break_law_datetime1",
                    "break_law_datetime2",
                    "inspect_datetime",
                    "is_direct_metal_firm",
                    "is_shadow_firm",
                    "is_risk_word_export_firm",
                    "has_dual_use_license",
                    "is_network_firm",
                    "is_manual_chk_metal",
                    "is_scope_metal_firm",
                    "sm_export_entry_cnt_3y",
                    "sm_export_value_3y",
                    "sm_export_dest_country_cnt_3y",
                    "shadow_export_entry_cnt_3y",
                    "shadow_export_value_3y",
                    "all_export_entry_cnt_3y",
                    "all_export_value_3y",
                    "sm_export_value_ratio_all_3y",
                    "sm_export_entry_ratio_all_3y",
                    "shadow_export_value_ratio_all_3y",
                    "sm_unit_price_avg_3y",
                    "sm_value_density_avg_3y",
                    "shadow_unit_price_avg_3y",
                    "sm_vs_shadow_unit_price_ratio",
                    "sm_air_ratio_3y",
                    "sm_sensitive_dest_ratio_3y",
                    "sm_trade_months_active_3y",
                    "sm_trade_duration_months_3y",
                    "sm_avg_monthly_entry_cnt_3y",
                    "sm_monthly_entry_std_3y",
                    "sm_entry_cnt_12m",
                    "sm_entry_cnt_6m",
                    "sm_value_12m",
                    "sm_value_6m",
                    "sm_value_growth_6m_vs_prev_6m",
                    "shadow_entry_cnt_12m",
                    "shadow_value_12m",
                    "owner_cnt_3y",
                    "agent_cnt_3y",
                    "oversea_buyer_cnt_3y",
                    "sm_oversea_buyer_cnt_3y",
                    "sm_oversea_buyer_ratio_3y",
                    "audit_case_cnt_3y",
                    "audit_smuggling_flag",
                    "seized_case_cnt_3y",
                    "seized_entry_ratio_3y",
                    "sm_seized_case_cnt_3y",
                    "post_policy_seized_cnt_1p5y",
                    "dual_use_license_flag",
                    "dual_use_license_cnt_3y",
                    "dual_use_license_sm_ratio_3y",
                    "dual_use_license_sensitive_dest_ratio",
                    "dual_use_license_post_policy_cnt"
            };

            List<String> selectParts = new java.util.ArrayList<>();
            selectParts.add("trade_co");
            for (String col : specs) {
                if (present.contains(col)) {
                    if (!"trade_co".equals(col)) {
                        selectParts.add(col);
                    }
                }
            }

            String baseWhere = "";
            List<Object> params = new java.util.ArrayList<>();
            List<String> likeColumns = new java.util.ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                if (present.contains("etps_name")) likeColumns.add("lower(etps_name) LIKE ?");
                if (present.contains("trade_co")) likeColumns.add("lower(trade_co) LIKE ?");
                if (present.contains("uniscid")) likeColumns.add("lower(uniscid) LIKE ?");
                if (present.contains("reg_prov_name")) likeColumns.add("lower(reg_prov_name) LIKE ?");
                if (present.contains("reg_city_name")) likeColumns.add("lower(reg_city_name) LIKE ?");
                for (int i = 0; i < likeColumns.size(); i++) params.add(like);
                if (!likeColumns.isEmpty()) baseWhere = " WHERE " + String.join(" OR ", likeColumns);
            }

            String selectSql = "SELECT " + String.join(", ", selectParts) + " FROM dual_use_items" + baseWhere + " ORDER BY trade_co LIMIT ? OFFSET ?";
            long total = params.isEmpty()
                    ? jdbcTemplate.queryForObject("SELECT count(*) FROM dual_use_items" + baseWhere, Long.class)
                    : jdbcTemplate.queryForObject("SELECT count(*) FROM dual_use_items" + baseWhere, params.toArray(), Long.class);

            int batch = 1000;
            int offset = 0;
            List<Map<String, Object>> all = new java.util.ArrayList<>();
            while (offset < total) {
                List<Object> p = new java.util.ArrayList<>(params);
                p.add(batch);
                p.add(offset);
                List<Map<String, Object>> rows = p.size() > 2
                        ? jdbcTemplate.queryForList(selectSql, p.toArray())
                        : jdbcTemplate.queryForList(selectSql, batch, offset);
                if (rows.isEmpty()) break;
                all.addAll(rows);
                offset += rows.size();
            }
            result.put("rows", all);
            result.put("total", total);
        } catch (Exception e) {
            result.put("rows", List.of());
            result.put("total", 0);
        }
        return result;
    }
}
