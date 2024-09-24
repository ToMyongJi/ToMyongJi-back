FROM openjdk:17-alpine

#ARG JAR_FILE=/build/libs/tomyongji-0.0.1-SNAPSHOT.jar
#
#COPY ${JAR_FILE} tomyongji.jar
#
#ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "tomyongji.jar"]
#

COPY . .

# nginx를 세팅하기 위해 nginx를 가져옵니다.
FROM nginx:latest

# 루트에 nginx.conf 파일을 생성해주세요.
# image에서 기본 제공되는 nginx.conf를 덮어 씌울 것입니다.
# image에 있는 conf의 위치를 알 수 있습니다.
COPY nginx.conf /etc/nginx/nginx.conf

# nginx가 제공할 file들을 nginx/html로 복사합니다.
COPY --from=build-stage /app /usr/share/nginx/html

# 원하는 포트를 열어주세요 443은 https를 위해 열어줍니다.
EXPOSE 8080 443

# 이제 세팅이 끝났으니 nginx를 실행합니다.
CMD ["nginx", "-g", "daemon off;"]