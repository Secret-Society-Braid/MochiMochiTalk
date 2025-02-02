FROM openjdk:17.0.2-jdk
LABEL authors="ranfa"

COPY app/build/libs/app-all.jar /

ENTRYPOINT ["java", "-jar", "/app-all.jar"]
