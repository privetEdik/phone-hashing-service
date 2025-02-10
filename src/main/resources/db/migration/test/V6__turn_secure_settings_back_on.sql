-- Включаем безопасные настройки обратно
ALTER SYSTEM SET fsync = on;
ALTER SYSTEM SET synchronous_commit = on;
ALTER SYSTEM SET full_page_writes = on;
SELECT pg_reload_conf();

-- Оптимизируем БД после вставки
VACUUM FULL ANALYZE;
