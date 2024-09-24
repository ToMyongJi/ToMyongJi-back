FROM openjdk:17-alpine

ARG JAR_FILE=/build/libs/tomyongji-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} tomyongji.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "tomyongji.jar"]


