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
                    "trade_co text," +
                    "etps_name text," +
                    "model_id bigint," +
                    "model_name text," +
                    "tag text," +
                    "applied_at timestamptz default now()," +
                    "tag_status text default 'active'," +
                    "project_status text default 'archived'," +
                    "tag_category text)" );
            jdbc.execute("do $$ begin begin execute 'alter table tag_relations add constraint uniq_tag_rel unique (trade_co, model_id, tag)'; exception when duplicate_object then null; end; end $$;");
            jdbc.execute("do $$ begin begin execute 'create index if not exists idx_tag_rel_trade_co on tag_relations(trade_co)'; exception when duplicate_object then null; end; end $$;");
            jdbc.execute("do $$ begin begin execute 'create index if not exists idx_tag_rel_model on tag_relations(model_id)'; exception when duplicate_object then null; end; end $$;");
            jdbc.execute("do $$ begin begin execute 'create index if not exists idx_tag_rel_etps on tag_relations(etps_name)'; exception when duplicate_object then null; end; end $$;");
            jdbc.execute("do $$\n" +
                    "begin\n" +
                    "  if exists (\n" +
                    "    select 1 from information_schema.columns\n" +
                    "    where table_schema = 'public'\n" +
                    "      and table_name   = 'tag_relations'\n" +
                    "      and column_name  = 'trade_co'\n" +
                    "      and data_type   <> 'text'\n" +
                    "  ) then\n" +
                    "    execute 'alter table tag_relations alter column trade_co type text using trade_co::text';\n" +
                    "  end if;\n" +
                    "end $$;");
            jdbc.execute("do $$\n" +
                    "begin\n" +
                    "  if not exists (\n" +
                    "    select 1 from information_schema.columns\n" +
                    "    where table_schema = 'public'\n" +
                    "      and table_name   = 'tag_relations'\n" +
                    "      and column_name  = 'tag_category'\n" +
                    "  ) then\n" +
                    "    begin\n" +
                    "      execute 'alter table tag_relations add column tag_category text';\n" +
                    "    exception when duplicate_column then null; end;\n" +
                    "  end if;\n" +
                    "end $$;");
        } catch (Exception ignored) {}

        try {
            jdbc.execute("create table if not exists enterprise_info (" +
                    "id bigserial primary key," +
                    "name text not null," +
                    "category text not null," +
                    "region text not null," +
                    "risk double precision not null" +
                    ")");
            jdbc.execute("create index if not exists idx_enterprise_info_name on enterprise_info(name)");
            jdbc.execute("create table if not exists enterprise_tags (" +
                    "enterprise_id bigint references enterprise_info(id) on delete cascade," +
                    "tag_id bigint references tags(id) on delete cascade," +
                    "created_by text," +
                    "created_at timestamptz default now()," +
                    "primary key (enterprise_id, tag_id)" +
                    ")");
        } catch (Exception ignored) {}
    }
}
