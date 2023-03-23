create table if not exists extensions
(
    name    varchar(255) not null,
    data    longblob,
    version bigint,
    primary key (name)
);
