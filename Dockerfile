FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :bootstrap:shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/bootstrap/build/libs/*-all.jar app.jar
EXPOSE 3003
ENTRYPOINT ["java", "-jar", "app.jar"]
