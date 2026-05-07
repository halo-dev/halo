CREATE
    TABLE
        IF NOT EXISTS extensions(
            name VARCHAR(255) NOT NULL COLLATE utf8mb4_bin,
            DATA longblob,
            version BIGINT,
            PRIMARY KEY(name)
        );
