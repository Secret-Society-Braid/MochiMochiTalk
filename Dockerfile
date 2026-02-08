FROM gradle:8.3-jdk17-alpine AS builder
LABEL authors="ranfa"

WORKDIR /workspace

COPY settings.gradle.kts ./
COPY app/build.gradle.kts app/
COPY gradle/libs.versions.toml gradle/
COPY native/hajimeapi4j.jar native/hajimeapi4j.jar

RUN gradle :app:dependencies --no-daemon

COPY app/src app/src
RUN gradle :app:shadowJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine

COPY --from=builder /workspace/app/build/libs/app-all.jar /app-all.jar

ENV VOICEVOX_API_HOST=127.0.0.1
ENV VOICEVOX_API_PORT=50021

ENTRYPOINT ["java", "-jar", "/app-all.jar"]
