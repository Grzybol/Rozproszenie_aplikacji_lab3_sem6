# url-cleaner/Dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/url-cleaner-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
