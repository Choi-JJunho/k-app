FROM gradle:8.14.3-jdk21 AS build
WORKDIR /workspace
COPY . .
ARG GRADLE_BUILD_ARGS=""
RUN ./gradlew :api:bootJar --no-daemon ${GRADLE_BUILD_ARGS}

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
