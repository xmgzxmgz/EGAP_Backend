-- pg_trgm 可选，由运维手动安装；此处不强依赖
create table if not exists tags (
  id bigserial primary key,
  name text not null unique,
  description text,
  val integer,
  color text,
  source text default 'manual',
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

create table if not exists enterprise_info (
  id bigserial primary key,
  name text not null,
  category text not null,
  region text not null,
  risk double precision not null
);

create index if not exists idx_enterprise_info_name on enterprise_info(name);
-- 如启用 pg_trgm，可在运维层创建 GIN 索引：enterprise_info(name gin_trgm_ops)

create table if not exists enterprise_tags (
  enterprise_id bigint references enterprise_info(id) on delete cascade,
  tag_id bigint references tags(id) on delete cascade,
  created_by text,
  created_at timestamptz default now(),
  primary key (enterprise_id, tag_id)
);

create table if not exists tuning_models (
  id bigserial primary key,
  name text not null unique,
  creator text,
  created_at timestamptz default now(),
  status text,
  meta text
);

-- dual_use_items 的副表：包含主表全部字段，并追加模型与调优标签
do $$
begin
  if not exists (select 1 from information_schema.tables where table_schema='public' and table_name='dual_use_items_tuned') then
    execute 'create table dual_use_items_tuned (like dual_use_items including all)';
    begin
      execute 'alter table dual_use_items_tuned add column model_id bigint';
    exception when duplicate_column then null; end;
    begin
      execute 'alter table dual_use_items_tuned add column model_name text';
    exception when duplicate_column then null; end;
    begin
      execute 'alter table dual_use_items_tuned add column tuned_meta jsonb default ''{}''::jsonb';
    exception when duplicate_column then null; end;
    begin
      execute 'alter table dual_use_items_tuned drop constraint if exists dual_use_items_tuned_pkey';
    exception when undefined_object then null; end;
    begin
      execute 'alter table dual_use_items_tuned add constraint dual_use_items_tuned_pk primary key (model_id, item_id)';
    exception when duplicate_object then null; end;
    begin
      execute 'alter table dual_use_items_tuned add constraint fk_dual_tuned_item foreign key (item_id) references dual_use_items(item_id) on delete cascade';
    exception when duplicate_object then null; end;
    begin
      execute 'alter table dual_use_items_tuned add constraint fk_dual_tuned_model foreign key (model_id) references tuning_models(id) on delete cascade';
    exception when duplicate_object then null; end;
    begin
      execute 'create index if not exists idx_dual_use_items_tuned_model on dual_use_items_tuned(model_id)';
    exception when duplicate_object then null; end;
  end if;
end $$;
