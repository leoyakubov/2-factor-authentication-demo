FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace/backend

COPY backend/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY backend/src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /workspace/backend/target/*.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
