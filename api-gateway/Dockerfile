ARG BUILDPLATFORM=linux/arm64
FROM --platform=$BUILDPLATFORM openjdk:21

WORKDIR /app

COPY build/libs/*.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]
