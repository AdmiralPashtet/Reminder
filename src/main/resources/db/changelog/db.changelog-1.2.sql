-- changeset pavel.vlezko:3
ALTER TABLE Reminders
ALTER COLUMN remind
  TYPE timestamp without time zone
  USING (remind AT TIME ZONE 'UTC' AT TIME ZONE 'Europe/Moscow');