CREATE TABLE IF NOT EXISTS phone_number_entity (
                                                   id           BIGSERIAL PRIMARY KEY,
                                                   phone_number VARCHAR(12),
                                                   hash_value   BYTEA
);

