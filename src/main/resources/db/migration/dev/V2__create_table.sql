-- Создание таблицы
CREATE TABLE IF NOT EXISTS phone_number_entity (
                                                   id            BIGSERIAL PRIMARY KEY,
                                                   phone_number  VARCHAR(12) NOT NULL UNIQUE,
                                                   hash_value    BYTEA       NOT NULL UNIQUE
);
