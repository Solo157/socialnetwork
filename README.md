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