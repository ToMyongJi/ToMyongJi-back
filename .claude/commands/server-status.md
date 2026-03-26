---
description: EC2 개발 서버의 Docker 컨테이너 상태 및 시스템 자원을 확인합니다.
usage: /server-status
---

# Server Status

EC2 개발 서버(`tomyongji-dev`)의 운영 상태를 Bash SSH로 확인합니다.

## 실행 절차

아래 SSH 명령들을 Bash 툴로 순서대로 실행합니다.

```bash
ssh -i "C:/Users/jinhy/.ssh/id_rsa" -o StrictHostKeyChecking=no ubuntu@3.38.75.240 "
  echo '=== Docker Containers ===' && \
  docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' && \
  echo '' && \
  echo '=== Resource Usage ===' && \
  docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}' && \
  echo '' && \
  echo '=== Disk Usage ===' && \
  df -h /
"
```

## 출력 해석
- 컨테이너 Status가 `Up`이 아닌 경우 이상 여부를 사용자에게 알립니다.
- CPU/Memory 사용률이 비정상적으로 높으면 주의 메시지를 표시합니다.
- 디스크 사용률이 80% 이상이면 경고합니다.
