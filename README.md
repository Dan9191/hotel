# Hotel Service (hotel-service)

## Swagger

Доступен при запуске

http://localhost:8081/webjars/swagger-ui/index.html#/

## Описание

Сервис для управления отелями и комнатами, включая создание, получение доступных и рекомендуемых комнат, проверку доступности и временную блокировку комнат для бронирования. Эндпоинты доступны в зависимости от ролей: `ROLE_ADMIN` для создания, `ROLE_BOOKING_SERVICE` и `ROLE_USER` для просмотра.

## Сборка

Требуется Java 21 и Gradle.

### Сборка
```shell
./gradlew clean build
```

### Запуск
```shell
./gradlew bootRun
```

## Конфигурация

Настройки в `src/main/resources/application.yaml`:

| Переменная                                | Значение по умолчанию                                            | Описание                                      |
|-------------------------------------------|------------------------------------------------------------------|-----------------------------------------------|
| SERVER_PORT                               | 8081                                                             | Порт сервиса                                  |
| SPRING_APPLICATION_NAME                   | hotel-service                                                    | Имя приложения                                |
| SPRING_R2DBC_URL                          | r2dbc:h2:mem:///hoteldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE | URL базы данных H2                            |
| SPRING_R2DBC_USERNAME                     | sa                                                               | Пользователь базы данных                      |
| SPRING_R2DBC_PASSWORD                     |                                                                  | Пароль базы данных                            |
| SPRING_FLYWAY_ENABLED                     | true                                                             | Включение Flyway миграций                     |
| SPRING_FLYWAY_LOCATIONS                   | classpath:db/migration                                           | Расположение миграционных скриптов            |
| SPRING_FLYWAY_BASELINE_ON_MIGRATE         | true                                                             | Базовая миграция при необходимости            |
| EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE     | http://localhost:8761/eureka/                                    | URL Eureka сервера                            |
| MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE | health,info,metrics,env,build,git                                | Экспонируемые эндпоинты Actuator              |

## Эндпоинты API

### 1. Создание отеля
- **Эндпоинт**: `POST /api/hotels`
- **Заголовки**: `Content-Type: application/json`, `X-Correlation-Id: <correlation-id>`, `Authorization: Bearer <jwt-token>`
- **Роли**: `ROLE_BOOKING_SERVICE`, `ROLE_ADMIN`
- **Тело запроса**:
  ```json
  {"name":"Test Hotel","address":"123 Test St"}
  ```
- **Пример**:
  ```bash
  curl -X POST http://localhost:8081/api/hotels -H "Content-Type: application/json" -H "X-Correlation-Id: test-123" -H "Authorization: Bearer <jwt-token>" -d '{"name":"Test Hotel","address":"123 Test St"}'
  ```
- **Ответ**:
  - 201 Created: JSON с созданным отелем (с id)
  - 400 Bad Request: `"Hotel with name Test Hotel already exists"`

### 2. Получение всех отелей
- **Эндпоинт**: `GET /api/hotels`
- **Заголовки**: `X-Correlation-Id: <correlation-id>`, `Authorization: Bearer <jwt-token>`
- **Роли**: `ROLE_BOOKING_SERVICE`, `ROLE_USER`, `ROLE_ADMIN`
- **Пример**:
  ```bash
  curl -X GET http://localhost:8081/api/hotels -H "X-Correlation-Id: test-123" -H "Authorization: Bearer <jwt-token>"
  ```
- **Ответ**:
  - 200 OK: Массив JSON с отелями

### 3. Создание комнаты
- **Эндпоинт**: `POST /api/rooms`
- **Заголовки**: `Content-Type: application/json`, `X-Correlation-Id: <correlation-id>`, `Authorization: Bearer <jwt-token>`
- **Роли**: `ROLE_BOOKING_SERVICE`, `ROLE_ADMIN`
- **Тело запроса**:
  ```json
  {"hotelId":1,"number":"101"}
  ```
- **Пример**:
  ```bash
  curl -X POST http://localhost:8081/api/rooms -H "Content-Type: application/json" -H "X-Correlation-Id: test-123" -H "Authorization: Bearer <jwt-token>" -d '{"hotelId":1,"number":"101"}'
  ```
- **Ответ**:
  - 201 Created: JSON с созданной комнатой (с id)
  - 400 Bad Request: `"Hotel with id 1 not found"`

### 4. Получение доступных комнат
- **Эндпоинт**: `GET /api/rooms?startDate=<start-date>&endDate=<end-date>`
- **Заголовки**: `X-Correlation-Id: <correlation-id>`, `Authorization: Bearer <jwt-token>`
- **Роли**: `ROLE_BOOKING_SERVICE`, `ROLE_USER`, `ROLE_ADMIN`
- **Параметры**: `startDate` (YYYY-MM-DD), `endDate` (YYYY-MM-DD)
- **Пример**:
  ```bash
  curl -X GET http://localhost:8081/api/rooms?startDate=2025-11-01&endDate=2025-11-03 -H "X-Correlation-Id: test-123" -H "Authorization: Bearer <jwt-token>"
  ```
- **Ответ**:
  - 200 OK: Массив JSON с доступными комнатами

### 5. Получение рекомендуемых комнат
- **Эндпоинт**: `GET /api/rooms/recommend?startDate=<start-date>&endDate=<end-date>`
- **Заголовки**: `X-Correlation-Id: <correlation-id>`, `Authorization: Bearer <jwt-token>`
- **Роли**: `ROLE_BOOKING_SERVICE`, `ROLE_USER`, `ROLE_ADMIN`
- **Параметры**: `startDate` (YYYY-MM-DD), `endDate` (YYYY-MM-DD)
- **Пример**:
  ```bash
  curl -X GET http://localhost:8081/api/rooms/recommend?startDate=2025-11-01&endDate=2025-11-03 -H "X-Correlation-Id: test-123" -H "Authorization: Bearer <jwt-token>"
  ```
- **Ответ**:
  - 200 OK: Массив JSON с рекомендуемыми комнатами (до 5)

### 6. Подтверждение доступности комнаты (временная бронь)
- **Эндпоинт**: `POST /api/rooms/{id}/confirm-availability`
- **Заголовки**: `Content-Type: application/json`, `X-Correlation-Id: <correlation-id>`, `Authorization: Bearer <jwt-token>`
- **Роли**: `ROLE_BOOKING_SERVICE`, `ROLE_ADMIN`
- **Тело запроса**:
  ```json
  {"requestId":"req-123","startDate":"2025-11-01","endDate":"2025-11-03"}
  ```
- **Пример**:
  ```bash
  curl -X POST http://localhost:8081/api/rooms/1/confirm-availability -H "Content-Type: application/json" -H "X-Correlation-Id: test-123" -H "Authorization: Bearer <jwt-token>" -d '{"requestId":"req-123","startDate":"2025-11-01","endDate":"2025-11-03"}'
  ```
- **Ответ**:
  - 200 OK: ID брони (число, например, 1)
  - 400 Bad Request: `"Room is already booked for these dates"` или `"Room is not available"`
  - 404 Not Found: `"Room with id 1 not found"`

### 7. Освобождение брони
- **Эндпоинт**: `POST /api/rooms/{id}/release`
- **Заголовки**: `X-Correlation-Id: <correlation-id>`, `Authorization: Bearer <jwt-token>`
- **Тело запроса**: Строка с requestId
- **Пример**:
  ```bash
  curl -X POST http://localhost:8081/api/rooms/1/release -H "Content-Type: application/json" -H "X-Correlation-Id: test-123" -H "Authorization: Bearer <jwt-token>" -d '"req-123"'
  ```
- **Ответ**:
  - 200 OK: (пустой ответ)
  - 400 Bad Request: `"Cannot release: invalid room or booking type"`

## Тестирование

Запуск тестов:
```shell
./gradlew test
```

Тесты используют H2DB in-memory, Flyway и Spring Boot Test.

### Зависимости для тестов
- `org.springframework.boot:spring-boot-starter-test`
- `io.r2dbc:r2dbc-h2`
- `com.h2database:h2`
- `io.mockk:mockk`
- `com.ninja-squad:spring-boot-starter-test-mockk`