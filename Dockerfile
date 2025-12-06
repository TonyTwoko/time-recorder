# ---------- Stage 1: Build + TESTS ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# Кэшируем зависимости
COPY pom.xml .
RUN mvn -q -e dependency:go-offline
# Копируем исходники
COPY src ./src
# Сборка + запуск тестов
RUN mvn clean package

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]