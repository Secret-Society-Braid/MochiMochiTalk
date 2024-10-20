FROM openjdk:latest
LABEL authors="ranfa"

COPY app/build/libs/app-all.jar /

ENTRYPOINT ["java", "-jar", "/app-all.jar"]
