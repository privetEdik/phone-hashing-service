-- 1) Увімкнути розширення pgcrypto (забезпечує digest(), encode() тощо).
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Создание таблицы
CREATE TABLE IF NOT EXISTS phone_number_entity (
                                                   id            BIGSERIAL PRIMARY KEY,
                                                   phone_number  VARCHAR(12) NOT NULL UNIQUE,
                                                   hash_value    BYTEA       NOT NULL UNIQUE
);

-- Создание индекса для быстрого поиска по хешу
--CREATE UNIQUE INDEX idx_hash_value ON phone_numbers (hash_value);
