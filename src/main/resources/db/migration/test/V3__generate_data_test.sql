
SET app.hash_algorithm = '${hash_algorithm}';
SET app.salt = '${salt}';

-- 4) Цикл на 50 000 000 (будьте готові до великого навантаження).
DO $$
    DECLARE
        i            BIGINT;
        phone_number TEXT;
        raw_hash     BYTEA;
    BEGIN

        -- Проверяем, входит ли в список допустимых
    IF current_setting('app.hash_algorithm') NOT IN ('SHA1', 'SHA2', 'SHA3') THEN
        RAISE EXCEPTION 'Unsupported group hash algorithm: %', current_setting('app.hash_algorithm');
    END IF;


        FOR i IN 1..500 LOOP
                -- Генерація номера телефону: 380 + 9 цифр (заповнення провідними нулями)
                phone_number := '380' || LPAD(i::TEXT, 9, '0');

                -- Залежно від алгоритму хешування:
                CASE current_setting('app.hash_algorithm')
                    WHEN 'SHA1' THEN
                        raw_hash := digest(phone_number || current_setting('app.salt'), 'sha1');
                    WHEN 'SHA2' THEN
                        raw_hash := digest(phone_number || current_setting('app.salt'), 'sha256');
                    WHEN 'SHA3' THEN
                        -- Увага: 'sha3-256' не завжди підтримується стандартною збіркою pgcrypto
                        raw_hash := digest(phone_number || current_setting('app.salt'), 'sha3-256');
--                     ELSE
--                         RAISE EXCEPTION 'Unsupported hash algorithm: %',
--                             current_setting('app.hash_algorithm');
                    END CASE;

                -- Вставка в таблицю phone_number_entity:
                INSERT INTO phone_number_entity(phone_number, hash_value)
                VALUES (phone_number, raw_hash);

                -- "Відпочинок" кожні 10 000 ітерацій (умовно, щоб не перевантажувати систему).
--                 IF i % 10000 = 0 THEN
--                     PERFORM pg_sleep(0.1);
--                 END IF;
            END LOOP;
    END
$$;
