FROM gradle:8-jdk21 AS build
WORKDIR /app

COPY settings.gradle.kts gradle.properties ./
COPY build.gradle.kts ./
COPY domain/build.gradle.kts domain/
COPY application/build.gradle.kts application/
COPY adapter-inbound/build.gradle.kts adapter-inbound/
COPY adapter-outbound/build.gradle.kts adapter-outbound/
COPY bootstrap/build.gradle.kts bootstrap/

RUN gradle dependencies --no-daemon --console=plain -q

COPY . .

RUN gradle :bootstrap:assemble --no-daemon -x test --console=plain

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/bootstrap/build/libs/*-all.jar app.jar
EXPOSE 3003
ENTRYPOINT ["java", "-jar", "app.jar"]
