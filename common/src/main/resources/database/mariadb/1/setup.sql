CREATE TABLE IF NOT EXISTS table_prefix.settings (
    unique_id VARCHAR(36) NOT NULL PRIMARY KEY,
    requests INT(1) NOT NULL DEFAULT 0,
    notifications INT(1) NOT NULL DEFAULT 0,
    chat INT(1) NOT NULL DEFAULT 0
);
