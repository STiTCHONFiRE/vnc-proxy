# VNC Proxy

Прокси преобразующий WebSocket трафик в TCP socket трафик, поддерживает множество адресов.

Минимальная версия Java: `17`.

## Конфигурация

Ддя каждого адреса задаётся свой идентификатор в формате `UUID`.
Для добавления адреса необходимо изменить конфигурационный файл `application.yml`.

Пример конфигурации:
```yaml
vnc:
  servers: >
   127.0.0.1:5900,
   192.168.50.47:5900
```

Порт сервера:

```yaml
server:
  port: 8085
```

Или с помощью параметров запуска при запуске `.jar` файла:

`--server.port=8085`


Настройка адреса провайдера аутентификации:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000
```

Или с помощью параметров запуска при запуске `.jar` файла:

`--spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000`

В качестве провайдера аутентификации подойдёт любой провайдер поддерживающий протокол `OAuth 2.0`.

## Сборка

Сборщик проекта: `maven`

Для сборки проекта, чтобы получить `.jar` файл, необходимо запустить фазу `package`.
```shell
mvn package
```
Также можно использовать `maven wrapper`, который уже установлен в репозитории, чтобы не устанавливать `maven` на локальный компьютер. 

```shell
./mvnw package
```

## Контейнеризация (Docker и т.п.)

Доступен для установки образ `vnc-proxy`, для создания контейнеров

```shell
docker pull ghcr.io/stitchonfire/vnc-proxy:latest
```

## Веб-Cокеты

Для подключения к веб-сокетам необходимо запросить одноразовый токен доступа и передать его в query параметрах при подключении. Токен дает доступ только к определенному уникальному идентификатору. 

Пример запроса:
```http request
@id = example
POST http://localhost:8080/api/v1/tokens/{{id}}
```

## Методы

Прокси предоставляет API для получения информации о зарегистрированных адресах и их идентификаторах:

```http request
GET http://localhost:8080/api/v1/vnc
```

Для подключения необходимо передавать jwt токен, полученный от провайдера аутентификации, в хедерах запроса.

Возвращает `json` в формате:

```json
[
    {
        "id": "d0b2ddeb-096e-41c6-8723-8c9e1a61e7b9",
        "ipAddressAndPort": "192.168.50.47:5900"
    },
    {
        "id": "f5be889a-f430-4102-ab65-d2b5858f15fc",
        "ipAddressAndPort": "127.0.0.1:5900"
    }
]
```
