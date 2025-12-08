package com.egap.backend.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class TuningModelSchemaInit {
    private final JdbcTemplate jdbc;

    public TuningModelSchemaInit(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        try {
            jdbc.execute("create table if not exists tuning_models (" +
                    "id bigserial primary key," +
                    "name text not null unique," +
                    "creator text," +
                    "created_at timestamptz default now()," +
                    "status text," +
                    "meta text)"
            );
        } catch (Exception ignored) {}

        try {
            jdbc.execute("do $$ begin\n" +
                    "  if not exists (select 1 from information_schema.tables where table_schema='public' and table_name='dual_use_items_tuned') then\n" +
                    "    execute 'create table dual_use_items_tuned (like dual_use_items including all)';\n" +
                    "    begin execute 'alter table dual_use_items_tuned add column model_id bigint'; exception when duplicate_column then null; end;\n" +
                    "    begin execute 'alter table dual_use_items_tuned add column model_name text'; exception when duplicate_column then null; end;\n" +
                    "    begin execute 'alter table dual_use_items_tuned add column tuned_meta jsonb default ''{}''::jsonb'; exception when duplicate_column then null; end;\n" +
                    "    begin execute 'alter table dual_use_items_tuned drop constraint if exists dual_use_items_tuned_pkey'; exception when undefined_object then null; end;\n" +
                    "    begin execute 'alter table dual_use_items_tuned add constraint dual_use_items_tuned_pk primary key (model_id, item_id)'; exception when duplicate_object then null; end;\n" +
                    "    begin execute 'alter table dual_use_items_tuned add constraint fk_dual_tuned_item foreign key (item_id) references dual_use_items(item_id) on delete cascade'; exception when duplicate_object then null; end;\n" +
                    "    begin execute 'alter table dual_use_items_tuned add constraint fk_dual_tuned_model foreign key (model_id) references tuning_models(id) on delete cascade'; exception when duplicate_object then null; end;\n" +
                    "    begin execute 'create index if not exists idx_dual_use_items_tuned_model on dual_use_items_tuned(model_id)'; exception when duplicate_object then null; end;\n" +
                    "  end if;\n" +
                    "end $$;");
        } catch (Exception ignored) {}

        try {
            jdbc.execute("create table if not exists tag_relations (" +
                    "id bigserial primary key," +
                    "item_id bigint," +
                    "enterprise_name text," +
                    "model_id bigint," +
                    "model_name text," +
                    "tag text," +
                    "applied_at timestamptz default now()," +
                    "tag_status text default 'active'," +
                    "project_status text default 'archived')");
            jdbc.execute("do $$ begin begin execute 'alter table tag_relations add constraint uniq_tag_rel unique (item_id, model_id, tag)'; exception when duplicate_object then null; end; end $$;");
            jdbc.execute("do $$ begin begin execute 'create index if not exists idx_tag_rel_model on tag_relations(model_id)'; exception when duplicate_object then null; end; end $$;");
            jdbc.execute("do $$ begin begin execute 'create index if not exists idx_tag_rel_item on tag_relations(item_id)'; exception when duplicate_object then null; end; end $$;");
        } catch (Exception ignored) {}
    }
}
