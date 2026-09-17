-- Схемы таблиц dialog-service.
-- Применяется автоматически при старте сервиса (spring.sql.init.mode: always).
-- Все операторы идемпотентны (IF NOT EXISTS).

-- Совпадают со схемой основного проекта (общая БД).
CREATE TABLE IF NOT EXISTS dialogs (
    id          VARCHAR(255) NOT NULL,
    user1_id    VARCHAR(255) NOT NULL,
    user2_id    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS dialog_messages (
    id          VARCHAR(255) NOT NULL,
    dialog_id   VARCHAR(255) NOT NULL,
    sender_id   VARCHAR(255) NOT NULL,
    receiver_id VARCHAR(255) NOT NULL,
    text        TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at     TIMESTAMP,
    PRIMARY KEY (dialog_id, id)
);

-- Миграция для БД, созданной основным проектом: таблица dialog_messages
-- существует без колонки read_at (нужна для пометки прочтения).
ALTER TABLE dialog_messages ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;

-- Outbox: события для получателя, сохраняемые в одной транзакции
-- с основным действием (создание/прочтение сообщения).
-- Записи не удаляются: статус NEW -> SENT после доставки в Kafka.
-- event_type: 'message.created' (+1 непрочитанных) или 'message.read' (-1).
-- id записи outbox одновременно eventId — ключ идемпотентности
-- в counter-service: уникален для каждого события.
-- ID — VARCHAR(64): Java-код пишет строки через setString(),
-- Postgres не делает неявный каст varchar -> uuid.
CREATE TABLE IF NOT EXISTS message_outbox (
    id            VARCHAR(64) NOT NULL,
    message_id    VARCHAR(64) NOT NULL,
    receiver_id   VARCHAR(64) NOT NULL,
    dialog_id     VARCHAR(64) NOT NULL,
    sender_id     VARCHAR(64) NOT NULL,
    event_type    VARCHAR(32) NOT NULL,
    status        VARCHAR(16) NOT NULL DEFAULT 'NEW',
    created_at    TIMESTAMP NOT NULL,
    sent_at       TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_message_outbox_pending
    ON message_outbox (created_at, id)
    WHERE status = 'NEW';
