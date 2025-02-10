-- Отключаем безопасные настройки (только временно)
ALTER SYSTEM SET fsync = off;
ALTER SYSTEM SET synchronous_commit = off;
ALTER SYSTEM SET full_page_writes = off;
SELECT pg_reload_conf();
