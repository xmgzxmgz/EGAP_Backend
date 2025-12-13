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
