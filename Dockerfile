FROM openjdk:21-slim

WORKDIR /app

COPY target/reminderproject-0.0.1.jar reminderproject.jar

ENTRYPOINT ["java", "-jar", "reminderproject.jar"]