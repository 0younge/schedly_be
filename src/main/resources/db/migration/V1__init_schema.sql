create table users (
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(255) not null,
    name varchar(100) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_users_email unique (email)
);

create table schedules (
    id uuid primary key,
    user_id uuid not null,
    title varchar(120) not null,
    start_at timestamp with time zone not null,
    end_at timestamp with time zone not null,
    memo varchar(1000),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_schedules_user foreign key (user_id) references users (id) on delete cascade,
    constraint ck_schedules_time_range check (end_at > start_at)
);

create index idx_schedules_user_time on schedules (user_id, start_at, end_at);
