# Stage 1: Build Java Application
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
# Pre-fetch dependencies to speed up subsequent builds
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production JRE Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Directory for SQLite persistent database
RUN mkdir -p /app/data

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
