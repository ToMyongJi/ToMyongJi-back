FROM eclipse-temurin:17-jdk-jammy

COPY build/libs/tomyongji-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-Xmx512m","-jar","-Dspring.profiles.active=dev", "app.jar"]