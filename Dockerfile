FROM openjdk:21-slim

WORKDIR /app

COPY target/ReminderProject-0.0.1.jar ReminderProject.jar

ENTRYPOINT ["java", "-jar", "ReminderProject.jar"]