-- Схемы таблиц counter-service.
-- Применяется автоматически при старте сервиса (spring.sql.init.mode: always).
-- Все операторы идемпотентны (IF NOT EXISTS).

-- Счётчик непрочитанных сообщений на пользователя.
CREATE TABLE IF NOT EXISTS user_message_counters (
    user_id          VARCHAR(64) PRIMARY KEY,
    unread_count     BIGINT NOT NULL DEFAULT 0,
    last_message_at  TIMESTAMP
);

-- Журнал обработанных событий (идемпотентность при повторной
-- доставке Kafka: повторное событие не увеличивает счётчик).
CREATE TABLE IF NOT EXISTS processed_counter_events (
    event_id      VARCHAR(64) PRIMARY KEY,
    processed_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
