create table if not exists extensions
(
    name    varchar(255) not null COLLATE utf8mb4_bin,
    data    longblob,
    version bigint,
    primary key (name)
);
