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
                    "Regulatory Authority",
                    "Registration Location",
                    "Registered Capital (10k CNY)",
                    "Paid-in Capital (10k CNY)",
                    "Legal Person Risk",
                    "Enterprise Type (Nature)",
                    "Current Year Import/Export Amount (10k CNY)",
                    "Past Three Years Import/Export Amount (10k CNY)",
                    "Current Year Import/Export Growth Rate",
                    "Current Year Tax Amount (10k CNY)",
                    "Past Three Years Tax Amount (10k CNY)",
                    "Supervision_Current Year Import/Export Amount (10k CNY)",
                    "Supervision_Past Three Years Import/Export Amount (10k CNY)",
                    "Supervision_Current Year Import/Export Growth Rate",
                    "Settlement Exchange Rate",
                    "Current Year Customs Enforcement Count",
                    "Previous Year Customs Enforcement Count",
                    "Current Year Anomaly Count",
                    "Past Three Years Anomaly Count",
                    "Customs Broker",
                    "Consignee Enterprise",
                    "Number of Associated Enterprises",
                    "Enterprise Type (Industry)",
                    "Industry Category",
                    "Specialized, Refined, Unique, New"
            };

            List<String> selectParts = new java.util.ArrayList<>();
            selectParts.add("item_id");
            for (String col : specs) {
                if (present.contains(col)) {
                    selectParts.add("\"" + col + "\" AS \"" + col + "\"");
                }
            }

            String baseWhere = "";
            List<Object> params = new java.util.ArrayList<>();
            List<String> likeColumns = new java.util.ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                if (present.contains("Consignee Enterprise")) likeColumns.add("lower(\"Consignee Enterprise\") LIKE ?");
                if (present.contains("Registration Location")) likeColumns.add("lower(\"Registration Location\") LIKE ?");
                if (present.contains("Industry Category")) likeColumns.add("lower(\"Industry Category\") LIKE ?");
                for (int i = 0; i < likeColumns.size(); i++) params.add(like);
                if (!likeColumns.isEmpty()) baseWhere = " WHERE " + String.join(" OR ", likeColumns);
            }

            String selectSql = "SELECT " + String.join(", ", selectParts) + " FROM dual_use_items" + baseWhere + " ORDER BY item_id LIMIT ? OFFSET ?";
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
        } catch (Exception ignored) {
            result.put("rows", List.of());
            result.put("total", 0);
        }
        return result;
        } catch (Exception ignored) {
            Map<String, Object> result = new HashMap<>();
            result.put("rows", List.of());
            result.put("total", 0);
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
                    "Regulatory Authority",
                    "Registration Location",
                    "Registered Capital (10k CNY)",
                    "Paid-in Capital (10k CNY)",
                    "Legal Person Risk",
                    "Enterprise Type (Nature)",
                    "Current Year Import/Export Amount (10k CNY)",
                    "Past Three Years Import/Export Amount (10k CNY)",
                    "Current Year Import/Export Growth Rate",
                    "Current Year Tax Amount (10k CNY)",
                    "Past Three Years Tax Amount (10k CNY)",
                    "Supervision_Current Year Import/Export Amount (10k CNY)",
                    "Supervision_Past Three Years Import/Export Amount (10k CNY)",
                    "Supervision_Current Year Import/Export Growth Rate",
                    "Settlement Exchange Rate",
                    "Current Year Customs Enforcement Count",
                    "Previous Year Customs Enforcement Count",
                    "Current Year Anomaly Count",
                    "Past Three Years Anomaly Count",
                    "Customs Broker",
                    "Consignee Enterprise",
                    "Number of Associated Enterprises",
                    "Enterprise Type (Industry)",
                    "Industry Category",
                    "Specialized, Refined, Unique, New"
            };

            List<String> selectParts = new java.util.ArrayList<>();
            selectParts.add("item_id");
            for (String col : specs) {
                if (present.contains(col)) {
                    selectParts.add("\"" + col + "\" AS \"" + col + "\"");
                }
            }

            String baseWhere = "";
            List<Object> params = new java.util.ArrayList<>();
            List<String> likeColumns = new java.util.ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                if (present.contains("Consignee Enterprise")) likeColumns.add("lower(\"Consignee Enterprise\") LIKE ?");
                if (present.contains("Registration Location")) likeColumns.add("lower(\"Registration Location\") LIKE ?");
                if (present.contains("Industry Category")) likeColumns.add("lower(\"Industry Category\") LIKE ?");
                for (int i = 0; i < likeColumns.size(); i++) params.add(like);
                if (!likeColumns.isEmpty()) baseWhere = " WHERE " + String.join(" OR ", likeColumns);
            }

            String selectSql = "SELECT " + String.join(", ", selectParts) + " FROM dual_use_items" + baseWhere + " ORDER BY item_id LIMIT ? OFFSET ?";
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
