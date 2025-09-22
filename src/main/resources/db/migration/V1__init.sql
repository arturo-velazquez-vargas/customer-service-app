create extension if not exists "uuid-ossp";

create table if not exists products (
    id bigserial primary key,
    external_id text unique,
    title text not null,
    price numeric(12,2),
    url text,
    variants jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

create index if not exists idx_products_title on products using gin (to_tsvector('simple', coalesce(title,'')));
create index if not exists idx_products_variants on products using gin (variants);
