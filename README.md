# Reminder Application
Приложение для отправки напоминаний по электронной почте и в Telegram. 

---

## 1. Prerequisites 

- **Java 21**
- **Maven 3.8.1+** (для сборки без Docker)
- **Docker Engine 20.10+** и **Docker Compose 2.6+**<br/>
Установка по инструкции: https://docs.docker.com/engine/install/  
- Создайте в корне проекта файл `.env` на основе [`.env.example`](./.env.example)

---

## 2. Конфигурация

В `.env` необходимо задать ключи и идентификаторы для:

- Google Mail API (Username / Password)
- Google OAuth2 (Client ID / Client Secret)
- Github OAuth2 (Client ID / Client Secret)
- Telegram Bot API (Bot Token)  

---
## 3. Запуск приложения

1. **Сборка проекта**  
   Выполняется из корня проекта:
   ```bash
   # Windows
   mvnw.cmd package -Dmaven.test.skip=true

   # Unix/Linux/Mac
   ./mvnw package -Dmaven.test.skip=true
   ```
2. **Запуск в Docker**
   `docker compose up`

Приложение будет доступно на http://localhost:8080.

---
## 4. Аутентификация и авторизация

В проекте используется OAuth 2.0 / OpenID Connect с Google и Github.
- Бэкенд не обрабатывает UI-перенаправления (например, страницы входа через браузер). Ожидается, что фронтенд получает ID Token (JWT) и передает его в заголовке `Authorization: Bearer <ID_TOKEN>`. 
- Для GitHub OAuth2 используется opaque token.

> [!TIP]
> Для ручного тестирования API удобно использовать Postman, получив (задав) заголовок Authorization.

---

## 5. Часовой пояс
- Приложение работает в GMT+3 часовом поясе (МСК) – необходимо это учитывать при планировании времени отправки напоминаний.

---

## 6. Бот телеграм

- Ссылка на бота: **http://t.me/rednimerbot**
- Для работы бота после регистрации в системе нужно указать telegramUsername, отправив PATCH запрос через API на url `/api/v1/users`:
```
{
  "reminderEmail": "email@mail.com",
  "telegramUsername": "telegramUsername"
}
```
- Для привязки чата к пользователю необходимо отправить в чат команду `/start`. 

---

## 7. User flow
1. При первом входе сервис создает пользователя. Логин - это email из провайдера OpenID.
2. Для настройки напоминаний предусмотрен endpoint `/api/v1/users` для PATCH запроса с телом:
```
{
  "reminderEmail": "email@mail.com",
  "telegramUsername": "telegramUsername"
}
```
3. Для выключения уведомлений из того или иного источника соответствующее поле должно быть установлено в `null`.
4. Ежеминутно приложение направляет запрос в БД для отправки уведомлений по расписанию.

> [!TIP]
> Документация API после запуска доступна без авторизации по адресу: http://localhost:8080/swagger-ui/index.html

![image](https://github.com/user-attachments/assets/2bb6ffa3-5daa-475d-9a48-418838a2aca5)



## 8. TODO
1. Добавить систему статусов (отправлено/не отправлено);
2. Реализовать retry-механику для неудачных попыток отправки сообщений.
