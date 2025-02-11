DELETE FROM phone_number_entity
WHERE id NOT IN (
    SELECT MIN(id) FROM phone_number_entity GROUP BY hash_value
);

-- Теперь можно наложить ограничения:
ALTER TABLE phone_number_entity
    ALTER COLUMN phone_number SET NOT NULL,
ALTER COLUMN hash_value SET NOT NULL,
    ADD CONSTRAINT unique_phone_number UNIQUE (phone_number),
    ADD CONSTRAINT unique_hash_value UNIQUE (hash_value);
