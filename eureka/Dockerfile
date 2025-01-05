FROM openjdk:21-jdk-alpine

WORKDIR /app

COPY eureka/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
