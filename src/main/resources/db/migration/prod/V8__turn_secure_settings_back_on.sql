-- Включаем безопасные настройки обратно
ALTER SYSTEM SET fsync = on;
ALTER SYSTEM SET synchronous_commit = on;
ALTER SYSTEM SET full_page_writes = on;



