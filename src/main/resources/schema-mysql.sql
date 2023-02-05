create table if not exists extensions
(
    name    varchar(255) not null,
    data    bigint,
    version bigint,
    primary key (name)
);
