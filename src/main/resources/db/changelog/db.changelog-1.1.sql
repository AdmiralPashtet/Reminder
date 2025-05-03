-- changeset pavel.vlezko:2
ALTER TABLE Users
    ADD COLUMN notification_email VARCHAR(128);
ALTER TABLE Users
    ADD COLUMN telegram_chat_id BIGINT;
ALTER TABLE Users
    ADD COLUMN telegram_username VARCHAR(128)