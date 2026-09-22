# ---- Etapa de build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Etapa de execução ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S votacao && adduser -S votacao -G votacao
COPY --from=build /app/target/*.jar app.jar
RUN chown -R votacao:votacao /app
USER votacao:votacao

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
