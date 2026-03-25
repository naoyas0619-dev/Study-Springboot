FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

CMD ["java", "-jar", "build/libs/task-api-0.0.1-SNAPSHOT.jar"]
