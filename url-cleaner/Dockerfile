# url-cleaner/Dockerfile

# Etap 1: budowanie JAR-a
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etap 2: finalny obraz
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/url-cleaner-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
