ARG BUILDPLATFORM=linux/arm64
FROM --platform=$BUILDPLATFORM openjdk:21

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
