FROM gradle:8.3-jdk17-alpine AS builder
LABEL authors="ranfa"

WORKDIR /workspace

COPY settings.gradle ./
COPY app/build.gradle app/
COPY native/hajimeapi4j.jar native/hajimeapi4j.jar

RUN gradle dependencies --no-daemon || true

COPY app/src app/src
RUN gradle :app:shadowJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine

COPY --from=builder /workspace/app/build/libs/app-all.jar /app-all.jar

ENTRYPOINT ["java", "-jar", "/app-all.jar"]
