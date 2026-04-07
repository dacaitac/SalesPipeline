# Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy gradle wrapper and configuration files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Copy source code
COPY src/ src/

# Set execution permission and build the application
RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]