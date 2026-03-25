# Base image with Java 17 to build and run the Spring Boot app.
FROM eclipse-temurin:17-jdk-jammy

# Work inside /app in the container.
WORKDIR /app

# Copy the entire project so Gradle can build it.
COPY . .

# Build the jar file. Tests are skipped here to keep image build simple.
RUN ./gradlew build -x test

# Start the application from the built jar.
CMD ["java", "-jar", "build/libs/task-api-0.0.1-SNAPSHOT.jar"]
