CREATE TABLE IF NOT EXISTS table_prefix.settings (
    unique_id VARCHAR(36) NOT NULL PRIMARY KEY,
    requests BOOLEAN NOT NULL DEFAULT TRUE,
    notifications BOOLEAN NOT NULL DEFAULT TRUE,
    chat BOOLEAN NOT NULL DEFAULT TRUE
);
