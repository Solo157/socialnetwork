# socialnetwork

----
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
ЗАПУСК ПРИЛОЖЕНИЯ С ШАРДИРОВАННОЙ БД
1. В корневой директории проекта выполнить: docker compose -f citus/docker-compose.yml up -d --pull always
поднимаются все воркеры, мастер, инстанс приложения.
2. Нужно создать таблицу и зарегистрировать ее, как распределенную таблицу со своим
ключом шардирования. Это нужно сделать вручную, т.к. не используется миграционный инструмент (Flyway, Liquibase)
Создание таблицы:
2.1 Зайти в координатор: psql -h localhost -p 5432 -U postgres
2.2 Добавить воркеров и координатора:
   SELECT citus_set_coordinator_host('master', 5432);
   SELECT citus_add_node('worker1', 5432);
   SELECT citus_add_node('worker2', 5432);
2.3
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

Как проверить, что шардирование работает:
1. Отправить сообщение в диалог между пользователями.
2. Выполнить на координаторе select * from dialog_messages; 
покажет одну строку.
Выполнить SELECT get_shard_id_for_distribution_column('dialog_messages', 'dialog_id');
   dialog_id - это идентификатор диалога, т.е. это шардированный ключ.
И тем самым получить номер/id шарды.
3. Зайти сначала в первую шарду: docker exec -it citus_worker1 psql -U postgres
и выполнить select * from dialog_messages_{номер_шарды};
   Зайти затем в вторую шарду: docker exec -it citus_worker2 psql -U postgres
   и выполнить select * from dialog_messages_{номер_шарды};
Убедиться, что только в одном из воркеров есть данный шард и только в нем появилась новая строка.
