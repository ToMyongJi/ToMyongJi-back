---
description: EC2 개발 서버에 임의 명령을 실행합니다.
usage: /server-exec <command>
---

# Server Exec

EC2 개발 서버(`tomyongji-dev`)에 Bash SSH로 임의 명령을 실행합니다.

## 실행 절차

1. 사용자가 전달한 `<command>`를 그대로 SSH로 실행합니다.

```bash
ssh -i "C:/Users/jinhy/.ssh/id_rsa" -o StrictHostKeyChecking=no ubuntu@3.38.75.240 "<command>"
```

2. 결과를 출력하고 필요시 간단히 해석합니다.

## 자주 쓰는 명령 예시

```bash
# 디렉터리 탐색
ls -la /home/ubuntu/tomyongji-dev/

# Docker 컨테이너 재시작
docker restart <container-name>

# docker-compose 재배포
cd /home/ubuntu/tomyongji-dev && docker compose pull && docker compose up -d

# 환경변수 확인 (민감 정보 주의)
cat /home/ubuntu/tomyongji-dev/.env
```

## 주의 사항
- `rm -rf`, `docker compose down` 등 파괴적인 명령은 실행 전 사용자에게 확인합니다.
- 민감 정보(비밀번호, 토큰)가 포함된 출력은 마스킹하여 표시합니다.
