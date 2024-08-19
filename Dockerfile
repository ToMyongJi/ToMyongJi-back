FROM openjdk:17-alpine

ARG JAR_FILE=/build/libs/tomyongji-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} tomyongji.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "tomyongji.jar"]

COPY src/main/resources/keystore.p12 /etc/ssl/
COPY ${JAR_FILE} app.jar
EXPOSE 8443
