# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
