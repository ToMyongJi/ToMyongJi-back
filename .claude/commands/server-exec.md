---
description: EC2 개발 서버에 임의 명령을 실행합니다.
usage: /server-exec <command>
---

# Server Exec

EC2 개발 서버(`tomyongji-dev`)에 SSH로 명령을 실행합니다.

```bash
ssh -i "C:/Users/jinhy/.ssh/id_rsa" -o StrictHostKeyChecking=no ubuntu@3.38.75.240 "<command>"
```

결과를 출력하고 필요시 간단히 해석합니다.

## 주의
- `rm -rf`, `docker compose down` 등 파괴적인 명령은 실행 전 사용자에게 확인합니다.
- 민감 정보가 포함된 출력은 마스킹하여 표시합니다.
