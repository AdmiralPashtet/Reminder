package ru.admiralpashtet.reminder.util;

public enum Message {

    REGISTRATION_SUCCESS("Registration successful. Now reminders will come to this bot."),
    USER_WITH_CURRENT_TG_NOT_FOUND("User with telegram @%s was not found in system."),
    REMINDER_DEFAULT_TEMPLATE("*%s*\n%s"),
    TG_BOT_SUPPORTS_ONLY_START_COMMAND("The bot only supports the /start command.");

    private final String template;

    Message(String template) {
        this.template = template;
    }

    public String get() {
        return template;
    }

    public String format(Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        return String.format(template, args);
    }
}