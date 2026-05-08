CREATE
    TABLE
        IF NOT EXISTS extensions(
            name VARCHAR(255) NOT NULL,
            DATA bytea,
            version BIGINT,
            PRIMARY KEY(name)
        );
