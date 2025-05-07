-- changeset pavel.vlezko:3
ALTER TABLE Reminders
ALTER COLUMN remind
    TYPE TIMESTAMP WITH TIME ZONE
    USING remind AT TIME ZONE 'UTC';

ALTER TABLE Users
    ADD COLUMN time_zone VARCHAR(64) NOT NULL;