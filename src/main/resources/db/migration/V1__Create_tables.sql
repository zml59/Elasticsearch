create table news(
id bigint primary key auto_increment,
title text,
content text,
url varchar(1000),
create_at timestamp default now(),
update_at timestamp default now()
)default CHARSET=utf8mb4;

create table unprocessed_links(
link varchar(1000)
);
create table processed_links(
link varchar(1000)
);