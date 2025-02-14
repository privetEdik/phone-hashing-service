SET app.hash_algorithm = '${hash_algorithm}';
SET app.salt = '${salt}';

DO $BODY$
    DECLARE
        i BIGINT;
        phone_number TEXT;
        raw_hash BYTEA;
        hash_alg TEXT;
        phone_numbers TEXT[] := '{}';
        hash_values BYTEA[] := '{}';
        batch_size CONSTANT INTEGER := 1000;
    BEGIN
        -- Определяем алгоритм хеширования заранее
        hash_alg := CASE current_setting('app.hash_algorithm')
                        WHEN 'SHA1' THEN 'sha1'
                        WHEN 'SHA2' THEN 'sha256'
                        WHEN 'SHA3' THEN 'sha3-256'
            END;

        IF hash_alg IS NULL THEN
            RAISE EXCEPTION 'Unsupported hash algorithm: %', current_setting('app.hash_algorithm');
        END IF;

        -- Цикл на 50 000 000 записей
        FOR i IN 1..50000000 LOOP
                -- Генерация номера телефона: 380 + 9 цифр (заполнение ведущими нулями)
                phone_number := '380' || LPAD(i::TEXT, 9, '0');

                -- Вычисление хеша
                raw_hash := digest(phone_number || current_setting('app.salt'), hash_alg);

                -- Добавляем данные в массивы
                phone_numbers := array_append(phone_numbers, phone_number);
                hash_values := array_append(hash_values, raw_hash);

                -- Когда массив достигает batch_size, выполняем вставку
                IF array_length(phone_numbers, 1) = batch_size THEN
                    INSERT INTO phone_number_entity (phone_number, hash_value)
                    SELECT unnest(phone_numbers), unnest(hash_values);

                    -- Очищаем массивы
                    phone_numbers := '{}';
                    hash_values := '{}';
                END IF;
            END LOOP;

        -- Вставка оставшихся записей, если они есть
        IF array_length(phone_numbers, 1) > 0 THEN
            INSERT INTO phone_number_entity (phone_number, hash_value)
            SELECT unnest(phone_numbers), unnest(hash_values);
        END IF;
    END;
$BODY$;