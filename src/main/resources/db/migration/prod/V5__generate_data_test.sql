SET app.hash_algorithm = '${hash_algorithm}';
SET app.salt = '${salt}';


DO $$
    DECLARE
        i            BIGINT;
        phone_number TEXT;
        raw_hash     BYTEA;
        hash_alg     TEXT;
    BEGIN
        -- Определяем алгоритм хеширования заранее
        hash_alg := CASE current_setting('app.hash_algorithm')
                        WHEN 'SHA1' THEN 'sha1'
                        WHEN 'SHA2' THEN 'sha256'
                        WHEN 'SHA3' THEN 'sha3-256'
--                         ELSE NULL
            END;

        -- Если алгоритм не поддерживается, выбрасываем исключение
        IF hash_alg IS NULL THEN
            RAISE EXCEPTION 'Unsupported hash algorithm: %', current_setting('app.hash_algorithm');
        END IF;

--  Цикл на 50 000 000
        FOR i IN 1..50000000 LOOP
                -- Генерация номера телефона: 380 + 9 цифр (заполнение ведущими нулями)
                phone_number := '380' || LPAD(i::TEXT, 9, '0');

                -- Вычисление хеша с заранее определенным алгоритмом
                raw_hash := digest(phone_number || current_setting('app.salt'), hash_alg);

                -- Вставка в таблицу
                INSERT INTO phone_number_entity(phone_number, hash_value)
                VALUES (phone_number, raw_hash);
            END LOOP;
    END
$$;
