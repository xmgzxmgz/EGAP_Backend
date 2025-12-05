package com.egap.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PolicyController {
    private final JdbcTemplate jdbcTemplate;

    public PolicyController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/policies")
    public Map<String, Object> getPolicies(@RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "20") int size,
                                           @RequestParam(value = "q", required = false) String q,
                                           @RequestParam(value = "all", required = false) String all) {
        if (all != null) {
            return getAllPolicies(q);
        }
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> present = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name='policies'",
                    String.class
            );
            String[] specs = new String[]{
                    "etps_name",
                    "industry_phy_name",
                    "industry_code_name",
                    "area_id",
                    "exist_status",
                    "common_busi",
                    "import_ratio",
                    "main_ciq_codes",
                    "main_parent_ciq",
                    "top_trade_countries",
                    "transport_mode",
                    "total_decl_amt",
                    "total_entry_cnt",
                    "avg_ticket_val",
                    "aeo_rating",
                    "delay_rate"
            };

            List<String> selectParts = new java.util.ArrayList<>();
            for (String col : specs) {
                if (present.contains(col)) {
                    selectParts.add("\"" + col + "\"");
                }
            }

            String baseWhere = "";
            List<Object> params = new java.util.ArrayList<>();
            List<String> likeColumns = new java.util.ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                if (present.contains("etps_name")) likeColumns.add("lower(\"etps_name\") LIKE ?");
                if (present.contains("industry_phy_name")) likeColumns.add("lower(\"industry_phy_name\") LIKE ?");
                if (present.contains("industry_code_name")) likeColumns.add("lower(\"industry_code_name\") LIKE ?");
                if (present.contains("area_id")) likeColumns.add("lower(\"area_id\") LIKE ?");
                for (int i = 0; i < likeColumns.size(); i++) params.add(like);
                if (!likeColumns.isEmpty()) baseWhere = " WHERE " + String.join(" OR ", likeColumns);
            }

            String selectSql = "SELECT " + String.join(", ", selectParts) + " FROM policies" + baseWhere + " LIMIT ? OFFSET ?";
            String countSql = "SELECT count(*) FROM policies" + baseWhere;

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
        } catch (Exception ignored) {
            result.put("rows", List.of());
            result.put("total", 0);
        }
        return result;
    }

    @GetMapping("/policies/all")
    public Map<String, Object> getAllPolicies(@RequestParam(value = "q", required = false) String q) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> present = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name='policies'",
                    String.class
            );
            String[] specs = new String[]{
                    "etps_name",
                    "industry_phy_name",
                    "industry_code_name",
                    "area_id",
                    "exist_status",
                    "common_busi",
                    "import_ratio",
                    "main_ciq_codes",
                    "main_parent_ciq",
                    "top_trade_countries",
                    "transport_mode",
                    "total_decl_amt",
                    "total_entry_cnt",
                    "avg_ticket_val",
                    "aeo_rating",
                    "delay_rate"
            };

            List<String> selectParts = new java.util.ArrayList<>();
            for (String col : specs) {
                if (present.contains(col)) {
                    selectParts.add("\"" + col + "\"");
                }
            }

            String baseWhere = "";
            List<Object> params = new java.util.ArrayList<>();
            List<String> likeColumns = new java.util.ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                if (present.contains("etps_name")) likeColumns.add("lower(\"etps_name\") LIKE ?");
                if (present.contains("industry_phy_name")) likeColumns.add("lower(\"industry_phy_name\") LIKE ?");
                if (present.contains("industry_code_name")) likeColumns.add("lower(\"industry_code_name\") LIKE ?");
                if (present.contains("area_id")) likeColumns.add("lower(\"area_id\") LIKE ?");
                for (int i = 0; i < likeColumns.size(); i++) params.add(like);
                if (!likeColumns.isEmpty()) baseWhere = " WHERE " + String.join(" OR ", likeColumns);
            }

            String selectSql = "SELECT " + String.join(", ", selectParts) + " FROM policies" + baseWhere + " ORDER BY 1 LIMIT ? OFFSET ?";
            long total = params.isEmpty()
                    ? jdbcTemplate.queryForObject("SELECT count(*) FROM policies" + baseWhere, Long.class)
                    : jdbcTemplate.queryForObject("SELECT count(*) FROM policies" + baseWhere, params.toArray(), Long.class);

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
