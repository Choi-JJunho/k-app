FROM gradle:8.14.3-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :api:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
