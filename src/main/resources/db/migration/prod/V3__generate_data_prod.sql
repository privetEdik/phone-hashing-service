-- 1) Увімкнути розширення pgcrypto (забезпечує digest(), encode() тощо).
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2) Створити таблицю, якщо вона ще не існує.
--    Відповідає сутності PhoneNumberEntity:
--    (id BIGSERIAL, number VARCHAR(12) UNIQUE, hash_value BYTEA UNIQUE).
-- CREATE TABLE IF NOT EXISTS phone_number_entity (
--                                                    id            BIGSERIAL PRIMARY KEY,
--                                                    phone_number  VARCHAR(12) NOT NULL UNIQUE,
--                                                    hash_value    BYTEA       NOT NULL UNIQUE
-- );

-- 3) Установити змінні середовища в рамках сеансу (можна винести за межі скрипту).
--    Якщо ви викликаєте цей скрипт через psql, можна передати їх як:
--       psql -v hashAlgo=SHA-1 -v saltVal=MY_SECRET_SALT -f script.sql
--    і тоді використати :hashAlgo / :saltVal замість 'SHA-1' нижче.
--    Або просто явно прописати:
SET app.hash_algorithm = 'SHA-1';
SET app.salt = 'salt_value';

-- 4) Цикл на 50 000 000 (будьте готові до великого навантаження).
DO $$
    DECLARE
        i            BIGINT;
        phone_number TEXT;
        raw_hash     BYTEA;
    BEGIN
        FOR i IN 1..500 LOOP
                -- Генерація номера телефону: 380 + 9 цифр (заповнення провідними нулями)
                phone_number := '380' || LPAD(i::TEXT, 9, '0');

                -- Залежно від алгоритму хешування:
                CASE current_setting('app.hash_algorithm')
                    WHEN 'SHA-1' THEN
                        raw_hash := digest(phone_number || current_setting('app.salt'), 'sha1');
                    WHEN 'SHA-2' THEN
                        raw_hash := digest(phone_number || current_setting('app.salt'), 'sha256');
                    WHEN 'SHA-3' THEN
                        -- Увага: 'sha3-256' не завжди підтримується стандартною збіркою pgcrypto
                        raw_hash := digest(phone_number || current_setting('app.salt'), 'sha3-256');
                    ELSE
                        RAISE EXCEPTION 'Unsupported hash algorithm: %',
                            current_setting('app.hash_algorithm');
                    END CASE;

                -- Вставка в таблицю phone_number_entity:
                INSERT INTO phone_number_entity(phone_number, hash_value)
                VALUES (phone_number, raw_hash);

                -- "Відпочинок" кожні 10 000 ітерацій (умовно, щоб не перевантажувати систему).
                IF i % 10000 = 0 THEN
                    PERFORM pg_sleep(0.1);
                END IF;
            END LOOP;
    END
$$;
