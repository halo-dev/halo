CREATE
    TABLE
        IF NOT EXISTS extensions(
            name VARCHAR(255) NOT NULL,
            DATA bytea,
            version BIGINT,
            PRIMARY KEY(name)
        );

CREATE
    INDEX IF NOT EXISTS extensions_name_pattern_ops ON
    extensions(
        name varchar_pattern_ops
    );
