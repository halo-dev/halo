CREATE
    TABLE
        IF NOT EXISTS extensions(
            name VARCHAR(255) NOT NULL,
            DATA BLOB,
            version BIGINT,
            PRIMARY KEY(name)
        );
