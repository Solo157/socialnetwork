# socialnetwork

О проекте. socialnetwork имеет несколько модулей:
1. [contract-module](contract-module) Модуль, который не является микросервисом, он хранит только некоторые общие данные
для остальных микросервисов. На текущий момент хранит файл [dialog.proto](contract-module/src/main/proto/dialog.proto) 
описывающий сущности/методы для работы gRPC.
2. [dialog-service](dialog-service) Микросервис, реализующий хранение диалогов между пользователями. На текущий момент
для хранения использует Redis. 
3. [socialnetwork](socialnetwork) Основном микросервис (скорее еще нераспилинный монолит). Хранит всю основную логику
по функционалу социальной сети.

Сервис socialnetwork и dialog-service общаются между собой через gRPC сервер, который поднимается на стороне dialog-service,
а socialnetwork является его клиентом. 
При запросах, связанных с диалогами, передается x-request-id, который реализует сквозное логирование между данными
микросервисами.

----
КОМПИЛЯЦИЯ И ПУШИНГ ОБРАЗА В РЕПОЗИТОРИЙ:
По умолчанию этого делать не нужно, т.к. образы берутся из github.
Если нужно скомпилировать и запушить в docker registry:
1. registry прописать в главном pom и пароль установить в .m2/settings.xml
2. Компилируем микросервисы: mvn clean install
3. Пушим в докер registry: MAVEN_OPTS="" mvn -pl socialnetwork,dialog-service jib:build

ЗАПУСК ПРИЛОЖЕНИЯ:
1. В корневой директории проекта выполнить: docker compose up -d --pull always
2. В коллекции /postman проверить работу ендпоинтов.

Если запуск осуществляется на удаленной машине, то выполнить: 
1. scp -i ~/.ssh/my_otus_id_rsa docker-compose.yml ubuntu@{IP}:socialnetwork/docker-compose.yml
2. Перейти на удаленную машину в директорию socialnetwork
3. Выполнить: docker compose up -d --pull always
4. В коллекции /postman проверить работу ендпоинтов.