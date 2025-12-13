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
                    "remark text," +
                    "meta jsonb default '{}'::jsonb)"
            );
            jdbc.execute("do $$ begin\n" +
                    "  if exists (select 1 from information_schema.columns where table_schema='public' and table_name='tuning_models' and column_name='meta' and data_type='text') then\n" +
                    "    execute 'alter table tuning_models alter column meta type jsonb using meta::jsonb';\n" +
                    "  end if;\n" +
                    "end $$;");
            jdbc.execute("do $$ begin\n" +
                    "  begin execute 'alter table tuning_models add column if not exists remark text'; exception when duplicate_column then null; end;\n" +
                    "end $$;");
        } catch (Exception ignored) {}

        try {
            jdbc.execute("create table if not exists dual_item_tags (" +
                    "id bigserial primary key," +
                    "name text not null unique," +
                    "creator text," +
                    "created_at timestamptz default now()," +
                    "status text," +
                    "meta jsonb default '{}'::jsonb)"
            );
            jdbc.execute("do $$ begin\n" +
                    "  if exists (select 1 from information_schema.columns where table_schema='public' and table_name='dual_item_tags' and column_name='meta' and data_type='text') then\n" +
                    "    execute 'alter table dual_item_tags alter column meta type jsonb using meta::jsonb';\n" +
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
            jdbc.execute("do $$ begin begin execute 'create index if not exists idx_tag_rel_enterprise on tag_relations(enterprise_name)'; exception when duplicate_object then null; end; end $$;");
            jdbc.execute("do $$ begin begin execute 'create index if not exists idx_tag_rel_applied_ms on tag_relations(applied_at_ms)'; exception when duplicate_object then null; end; end $$;");
        } catch (Exception ignored) {}
    }
}
