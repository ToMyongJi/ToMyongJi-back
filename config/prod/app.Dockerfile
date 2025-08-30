FROM openjdk:17-alpine

RUN apk add --no-cache curl

COPY build/libs/tomyongji-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-Xmx512m","-jar","-Dspring.profiles.active=prod", "app.jar"]