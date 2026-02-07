FROM openjdk:17.0.2-jdk
LABEL authors="ranfa"

COPY app/build/libs/app-all.jar /

ENV VOICEVOX_API_HOST=127.0.0.1
ENV VOICEVOX_API_PORT=50021

ENTRYPOINT ["java", "-jar", "/app-all.jar"]
