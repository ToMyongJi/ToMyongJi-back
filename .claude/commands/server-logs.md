---
description: EC2 개발 서버의 애플리케이션 로그를 조회합니다.
usage: /server-logs [lines]
---

# Server Logs

EC2 개발 서버(`tomyongji-dev`)의 로그를 Bash SSH로 조회합니다.

## 실행 절차

1. 사용자가 `[lines]` 인수를 넘겼으면 해당 줄 수만큼, 없으면 기본 100줄을 조회합니다.
2. 아래 SSH 명령을 Bash 툴로 실행합니다.

```bash
# 최근 N줄 조회
ssh -i "C:/Users/jinhy/.ssh/id_rsa" -o StrictHostKeyChecking=no ubuntu@3.38.75.240 \
  "tail -n <lines> /home/ubuntu/tomyongji-dev/app-logs/app.log"

# 실시간 스트림이 필요한 경우 (사용자가 'follow' 또는 '-f' 를 요청한 경우)
ssh -i "C:/Users/jinhy/.ssh/id_rsa" -o StrictHostKeyChecking=no ubuntu@3.38.75.240 \
  "tail -f /home/ubuntu/tomyongji-dev/app-logs/app.log"
```

3. 로그 내용을 출력하고, ERROR/WARN 레벨이 있으면 하이라이트하여 요약합니다.

## 주의 사항
- 실시간 스트림(`-f`)은 무한 대기이므로 사용자가 명시적으로 요청할 때만 사용합니다.
- 로그 파일 경로가 없으면 `ls /home/ubuntu/tomyongji-dev/app-logs/`로 파일 목록을 먼저 확인합니다.
