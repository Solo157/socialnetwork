# socialnetwork

КОМПИЛЯЦИЯ И ПУШИНГ ОБРАЗА В РЕПОЗИТОРИЙ:
По умолчанию этого делать не нужно, т.к. образы берутся из github.
Если нужно скомпилировать и запушить в docker registry:
1. registry прописать в главном pom и пароль установить в .m2/settings.xml
2. Компилируем микросервисы: mvn clean install
3. Пушим в докер registry: mvn jib:build

docker compose down - удалить образы compose.

ЗАПУСК ПРИЛОЖЕНИЯ:
1. В корневой директории проекта выполнить: docker compose up -d --pull always
2. В коллекции /postman проверить работу ендпоинтов.



----
docker compose -f citus/docker-compose.yml up master worker1 worker2 -d --pull always
docker compose -f citus/docker-compose.yml up
docker compose up master worker1 worker2
STEP 2 — инициализация Citus
Подключаешься к master:
psql -h localhost -p 5432 -U postgres
И выполняешь:
SELECT citus_add_node('worker1', 5432);
SELECT citus_add_node('worker2', 5432);

-- V1__create_dialog_message.sql
CREATE TABLE dialog_messages (
id VARCHAR(255) NOT NULL,
dialog_id VARCHAR(255) NOT NULL,
sender_id VARCHAR(255) NOT NULL,
receiver_id VARCHAR(255) NOT NULL,
text TEXT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (dialog_id, id)
);

SELECT create_distributed_table('dialog_messages', 'dialog_id');

посмотреть шард по id
SELECT get_shard_id_for_distribution_column('dialog_message', 2);

select * from dialog_message_102023;
SELECT shardid, nodename FROM pg_dist_shard_placement;

CREATE TABLE dialog_message (...);
SELECT create_distributed_table('dialog_message', 'dialog_id');