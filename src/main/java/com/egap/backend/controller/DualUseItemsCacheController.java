package com.egap.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/dual_use_items_cache")
public class DualUseItemsCacheController {
    private final JdbcTemplate jdbc;

    public DualUseItemsCacheController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        try {
            Number tsNum = (Number) body.get("timestamp");
            Boolean boost = null;
            try { boost = (Boolean) body.get("boost"); } catch (Exception ignored) {}
            List<Map<String, Object>> rows = castRows(body.get("rows"));
            List<String> columnsArg = castColumns(body.get("columns"));
            List<List<Object>> values = castValues(body.get("values"));
            boolean hasRows = rows != null && !rows.isEmpty();
            boolean hasValues = values != null && !values.isEmpty();
            boolean hasColumns = columnsArg != null && !columnsArg.isEmpty();
            if (tsNum == null || !hasColumns || (!hasRows && !hasValues)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(Map.of("error", "invalid payload")));
            }

            jdbc.execute("create schema if not exists dual_use_items_cache");

            String base = "dual_use_items_" + tsNum.longValue() + ((boost != null && boost) ? "_boost" : "");
            String table = ensureUniqueTableName(base);

            List<String> inferredCols = deriveColumns(hasRows ? rows : Collections.emptyList(), columnsArg);
            List<List<Object>> effectiveValues = hasValues ? values : valuesFromRows(rows, inferredCols);
            Map<String, String> colTypes = inferTypesFromValues(effectiveValues, inferredCols);

            String ddl = buildCreateTableDDL("dual_use_items_cache", table, inferredCols, colTypes);
            jdbc.execute(ddl);

            int count = batchInsertValues("dual_use_items_cache", table, inferredCols, colTypes, effectiveValues);
            
            Map<String, Object> r = new HashMap<>();
            r.put("table", table);
            r.put("schema", "dual_use_items_cache");
            r.put("columns", inferredCols);
            r.put("count", count);
            return ResponseEntity.ok(r);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(Map.of("error", String.valueOf(ex.getMessage()))));
        }
    }

    private static List<Map<String, Object>> castRows(Object o) {
        if (o instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object e : list) {
                if (e instanceof Map) {
                    //noinspection unchecked
                    out.add((Map<String, Object>) e);
                }
            }
            return out;
        }
        return null;
    }

    private static List<String> castColumns(Object o) {
        if (o instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object e : list) {
                if (e != null) out.add(String.valueOf(e));
            }
            return out;
        }
        return null;
    }

    private static List<List<Object>> castValues(Object o) {
        if (o instanceof List<?> list) {
            List<List<Object>> out = new ArrayList<>();
            for (Object row : list) {
                if (row instanceof List<?> rv) {
                    List<Object> one = new ArrayList<>();
                    for (Object v : rv) one.add(v);
                    out.add(one);
                } else if (row instanceof Object[] arr) {
                    List<Object> one = new ArrayList<>();
                    Collections.addAll(one, arr);
                    out.add(one);
                }
            }
            return out;
        } else if (o instanceof Object[] arr) {
            List<List<Object>> out = new ArrayList<>();
            for (Object row : arr) {
                if (row instanceof Object[] inner) {
                    List<Object> one = new ArrayList<>();
                    Collections.addAll(one, inner);
                    out.add(one);
                }
            }
            return out;
        } else if (o != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = mapper.writeValueAsString(o);
                List raw = mapper.readValue(json, List.class);
                List<List<Object>> out = new ArrayList<>();
                for (Object row : raw) {
                    if (row instanceof List<?> rv) {
                        List<Object> one = new ArrayList<>();
                        for (Object v : rv) one.add(v);
                        out.add(one);
                    } else if (row instanceof Object[] inner) {
                        List<Object> one = new ArrayList<>();
                        Collections.addAll(one, inner);
                        out.add(one);
                    } else {
                        // ignore non-array rows to avoid错列
                    }
                }
                return out;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String ensureUniqueTableName(String base) {
        String name = base;
        int retry = 0;
        while (true) {
            String regclass = jdbc.queryForObject("select to_regclass(?)", String.class, "dual_use_items_cache." + quoteIdent(name));
            if (regclass == null) return name;
            name = base + "_" + randomSuffix(4);
            if (++retry > 8) return name;
        }
    }

    private static String randomSuffix(int n) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < n; i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        return sb.toString();
    }

    private static String sanitizeColumn(String s) {
        if (s == null) return "col";
        String t = s.replace("\"", "");
        t = t.replaceAll("[\\p{Cntrl}]+", "");
        t = t.trim();
        return t.isEmpty() ? "col" : t;
    }

    private static String quoteIdent(String ident) {
        return '"' + ident.replace("\"", "") + '"';
    }

    private static List<String> deriveColumns(List<Map<String, Object>> rows, List<String> provided) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (provided != null && !provided.isEmpty()) {
            for (String c : provided) set.add(sanitizeColumn(c));
        } else {
            for (Map<String, Object> r : rows) {
                for (String k : r.keySet()) set.add(sanitizeColumn(k));
            }
        }
        return new ArrayList<>(set);
    }

    private static final Pattern INT_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern DEC_PATTERN = Pattern.compile("^-?\\d+\\.\\d+$");

    private static Map<String, String> inferTypes(List<Map<String, Object>> rows, List<String> columns) {
        Map<String, String> types = new HashMap<>();
        int N = Math.min(rows.size(), 100);
        for (String col : columns) {
            boolean allNull = true;
            boolean allInt = true;
            boolean anyDec = false;
            boolean allBool = true;
            boolean hasBooleanInstance = false;
            boolean anyTs = false;
            for (int i = 0; i < N; i++) {
                Object v = rows.get(i).get(col);
                if (v == null) continue;
                allNull = false;
                if (v instanceof Number num) {
                    allBool = false;
                    if (num instanceof Integer || num instanceof Long) {
                        // keep
                    } else {
                        anyDec = true;
                    }
                } else if (v instanceof Boolean) {
                    hasBooleanInstance = true;
                } else {
                    String s = String.valueOf(v);
                    if (INT_PATTERN.matcher(s).matches()) {
                        // integer-like
                        allBool = false;
                    } else if (DEC_PATTERN.matcher(s).matches()) {
                        anyDec = true;
                        allInt = false;
                        allBool = false;
                    } else if (isTimestamp(s)) {
                        anyTs = true;
                        allInt = false;
                        allBool = false;
                    } else if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
                        // boolean-like
                    } else {
                        allInt = false;
                        allBool = false;
                    }
                }
            }
            String pgType;
            if (allNull) pgType = "text";
            else if (anyTs) pgType = "timestamptz";
            else if (allBool && hasBooleanInstance) pgType = "boolean";
            else if (anyDec) pgType = "double precision";
            else if (allInt) pgType = "bigint";
            else pgType = "text";
            types.put(col, pgType);
        }
        return types;
    }

    private static Map<String, String> inferTypesFromValues(List<List<Object>> values, List<String> columns) {
        Map<String, String> types = new HashMap<>();
        int N = Math.min(values.size(), 100);
        for (int ci = 0; ci < columns.size(); ci++) {
            boolean allNull = true;
            boolean allInt = true;
            boolean anyDec = false;
            boolean allBool = true;
            boolean hasBooleanInstance = false;
            boolean anyTs = false;
            for (int r = 0; r < N; r++) {
                List<Object> row = values.get(r);
                Object v = ci < row.size() ? row.get(ci) : null;
                if (v == null) continue;
                allNull = false;
                if (v instanceof Number num) {
                    allBool = false;
                    if (num instanceof Integer || num instanceof Long) {
                        // keep
                    } else {
                        anyDec = true;
                    }
                } else if (v instanceof Boolean) {
                    hasBooleanInstance = true;
                } else {
                    String s = String.valueOf(v);
                    if (INT_PATTERN.matcher(s).matches()) {
                        // integer-like
                        allBool = false;
                    } else if (DEC_PATTERN.matcher(s).matches()) {
                        anyDec = true;
                        allInt = false;
                        allBool = false;
                    } else if (isTimestamp(s)) {
                        anyTs = true;
                        allInt = false;
                        allBool = false;
                    } else if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
                        // boolean-like
                    } else {
                        allInt = false;
                        allBool = false;
                    }
                }
            }
            String pgType;
            if (allNull) pgType = "text";
            else if (anyTs) pgType = "timestamptz";
            else if (allBool && hasBooleanInstance) pgType = "boolean";
            else if (anyDec) pgType = "double precision";
            else if (allInt) pgType = "bigint";
            else pgType = "text";
            types.put(columns.get(ci), pgType);
        }
        return types;
    }

    private static boolean isTimestamp(String s) {
        try {
            if (s == null || s.isBlank()) return false;
            if (INT_PATTERN.matcher(s).matches()) {
                if (s.length() == 13) { Instant.ofEpochMilli(Long.parseLong(s)); return true; }
                if (s.length() == 10) { Instant.ofEpochSecond(Long.parseLong(s)); return true; }
            }
            Instant.parse(s);
            return true;
        } catch (Exception ignored) { return false; }
    }

    private static String buildCreateTableDDL(String schema, String table, List<String> columns, Map<String, String> colTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ").append(schema).append('.').append(quoteIdent(table)).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(',');
            String c = columns.get(i);
            sb.append(quoteIdent(c)).append(' ').append(colTypes.get(c));
        }
        sb.append(')');
        return sb.toString();
    }

    private int batchInsert(String schema, String table, List<String> columns, Map<String, String> types, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) return 0;
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(schema).append('.').append(quoteIdent(table)).append('(');
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(quoteIdent(columns.get(i)));
        }
        sb.append(") values (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
        sb.append(')');
        String sql = sb.toString();
        List<Object[]> batch = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Object[] vals = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                String c = columns.get(i);
                Object v = r.get(c);
                vals[i] = convertValue(types.get(c), v);
            }
            batch.add(vals);
        }
        jdbc.batchUpdate(sql, batch);
        return rows.size();
    }

    private static List<List<Object>> valuesFromRows(List<Map<String, Object>> rows, List<String> columns) {
        List<List<Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            List<Object> one = new ArrayList<>(columns.size());
            for (String c : columns) {
                Object v = null;
                if (r.containsKey(c)) v = r.get(c);
                else {
                    for (String k : r.keySet()) {
                        if (sanitizeColumn(k).equals(c)) { v = r.get(k); break; }
                    }
                }
                one.add(v);
            }
            out.add(one);
        }
        return out;
    }

    private int batchInsertValues(String schema, String table, List<String> columns, Map<String, String> types, List<List<Object>> values) {
        if (values.isEmpty()) return 0;
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(schema).append('.').append(quoteIdent(table)).append('(');
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(quoteIdent(columns.get(i)));
        }
        sb.append(") values (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
        sb.append(')');
        String sql = sb.toString();
        List<Object[]> batch = new ArrayList<>();
        for (List<Object> row : values) {
            Object[] vals = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                Object v = i < row.size() ? row.get(i) : null;
                String t = types.get(columns.get(i));
                vals[i] = convertValue(t, v);
            }
            batch.add(vals);
        }
        jdbc.batchUpdate(sql, batch);
        return values.size();
    }

    private static Object convertValue(String pgType, Object v) {
        if (v == null) return null;
        try {
            switch (String.valueOf(pgType)) {
                case "bigint": {
                    if (v instanceof Number n) return n.longValue();
                    return Long.parseLong(String.valueOf(v));
                }
                case "double precision": {
                    if (v instanceof Number n) return n.doubleValue();
                    return Double.parseDouble(String.valueOf(v));
                }
                case "boolean": {
                    if (v instanceof Boolean b) return b;
                    String s = String.valueOf(v);
                    return ("true".equalsIgnoreCase(s) || "1".equals(s));
                }
                case "timestamptz": {
                    String s = String.valueOf(v);
                    if (INT_PATTERN.matcher(s).matches()) {
                        if (s.length() == 13) return Timestamp.from(Instant.ofEpochMilli(Long.parseLong(s)));
                        if (s.length() == 10) return Timestamp.from(Instant.ofEpochSecond(Long.parseLong(s)));
                    }
                    return Timestamp.from(Instant.parse(s));
                }
                default: return String.valueOf(v);
            }
        } catch (Exception e) {
            return String.valueOf(v);
        }
    }
}
